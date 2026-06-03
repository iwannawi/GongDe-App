package com.gongde.app.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 统一数据仓库，封装所有数据源访问
 * ViewModel 只通过此类访问数据
 */
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
        val existing = historyDao.getAll().find { it.date == today }
        val newCount = (existing?.count ?: 0) + count
        historyDao.upsert(DailyHistory(today, newCount))
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

    // ==================== 成就 ====================

    val allAchievements: List<Achievement> = achievementStore.allAchievements

    fun isUnlocked(id: String) = achievementStore.isUnlocked(id)

    suspend fun checkAndUnlock(totalCount: Int, todayCount: Int): List<Achievement> {
        return achievementStore.checkAndUnlock(totalCount, todayCount)
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
