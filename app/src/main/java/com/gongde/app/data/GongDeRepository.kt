package com.gongde.app.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class GongDeRepository(
    private val meritStore: MeritStore,
    private val historyDao: HistoryDao,
    private val prefsStore: PreferencesStore,
    private val achievementStore: AchievementStore
) {
    private val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE

    // ==================== 功德计数 ====================

    val totalCount: Int get() = meritStore.totalCount
    val todayCount: Int get() = meritStore.todayCount

    fun incrementMerit(): Pair<Int, Int> = meritStore.increment()

    fun resetMerit() = meritStore.reset()

    // ==================== 历史记录（Room） ====================

    suspend fun recordMerit(count: Int = 1) {
        val today = LocalDate.now().format(dateFormat)
        val updated = historyDao.incrementCount(today, count)
        if (updated == 0) {
            val inserted = historyDao.insertIgnore(DailyHistory(today, count))
            if (inserted == -1L) {
                historyDao.incrementCount(today, count)
            }
        }
    }

    suspend fun getRecentDays(n: Int): List<Pair<String, Int>> {
        val today = LocalDate.now()
        val fromDate = today.minusDays(n.toLong() - 1).format(dateFormat)
        val records = historyDao.getFrom(fromDate).associateBy { it.date }
        return (0 until n).map { i ->
            val date = today.minusDays(i.toLong()).format(dateFormat)
            date to (records[date]?.count ?: 0)
        }
    }

    suspend fun getWeekTotal(): Int {
        val fromDate = LocalDate.now().minusDays(6).format(dateFormat)
        return historyDao.getTotalFrom(fromDate)
    }

    suspend fun getMonthTotal(): Int {
        val fromDate = LocalDate.now().minusDays(29).format(dateFormat)
        return historyDao.getTotalFrom(fromDate)
    }

    suspend fun cleanupOldHistory(keepDays: Int = 90) {
        val cutoff = LocalDate.now().minusDays(keepDays.toLong()).format(dateFormat)
        historyDao.deleteOlderThan(cutoff)
    }

    // ==================== 用户偏好（DataStore） ====================

    suspend fun getHapticEnabled() = prefsStore.getHapticEnabled()
    suspend fun setHapticEnabled(value: Boolean) = prefsStore.setHapticEnabled(value)

    suspend fun getSwitchType() = prefsStore.getSwitchType()
    suspend fun setSwitchType(value: String) = prefsStore.setSwitchType(value)

    suspend fun getThemeId() = prefsStore.getThemeId()
    suspend fun setThemeId(value: String) = prefsStore.setThemeId(value)
    suspend fun getFirstPressCompleted() = prefsStore.getFirstPressCompleted()
    suspend fun setFirstPressCompleted(value: Boolean) = prefsStore.setFirstPressCompleted(value)
    suspend fun getDailyGoalCompletedDate() = prefsStore.getDailyGoalCompletedDate()
    suspend fun setDailyGoalCompletedDate(value: String) = prefsStore.setDailyGoalCompletedDate(value)
    suspend fun getStreak() = prefsStore.getStreak()

    // ==================== 成就 ====================

    val allAchievements: List<Achievement> = achievementStore.allAchievements

    fun isUnlocked(id: String) = achievementStore.isUnlocked(id)

    suspend fun checkAndUnlock(totalCount: Int, todayCount: Int): List<Achievement> {
        val streak = prefsStore.getStreak()
        return achievementStore.checkAndUnlock(totalCount, todayCount, streak)
    }

    suspend fun getCompletedAchievementIds(totalCount: Int, todayCount: Int): Set<String> {
        val streak = prefsStore.getStreak()
        return achievementStore.getCompletedIds(totalCount, todayCount, streak)
    }

    suspend fun updateStreak() {
        val today = LocalDate.now().format(dateFormat)
        val lastActive = prefsStore.getLastActiveDate()
        if (lastActive == today) return

        val currentStreak = prefsStore.getStreak()
        if (lastActive.isNotEmpty()) {
            val daysBetween = ChronoUnit.DAYS.between(
                LocalDate.parse(lastActive, dateFormat), LocalDate.now()
            )
            val newStreak = if (daysBetween == 1L) currentStreak + 1 else 1
            prefsStore.setStreak(newStreak)
        } else {
            prefsStore.setStreak(1)
        }
        prefsStore.setLastActiveDate(today)
    }
}
