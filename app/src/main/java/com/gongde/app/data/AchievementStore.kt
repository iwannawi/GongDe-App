package com.gongde.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val condition: String,
)

class AchievementStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("achievement_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_UNLOCKED_JSON = "unlocked_json"
        private const val KEY_LAST_ACTIVE_DATE = "last_active_date"
        private const val KEY_STREAK = "current_streak"
        private val DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE

        val ACHIEVEMENTS: List<Achievement> = listOf(
            Achievement("first_merit", "初入佛门", "累计获得1次功德", "🌱", "totalCount >= 1"),
            Achievement("merit_100", "功德小成", "累计获得100次功德", "🏅", "totalCount >= 100"),
            Achievement("merit_1000", "功德精进", "累计获得1000次功德", "🏆", "totalCount >= 1000"),
            Achievement("merit_10000", "功德无量", "累计获得10000次功德", "👑", "totalCount >= 10000"),
            Achievement("daily_100", "一日百善", "单日获得100次功德", "⚡", "todayCount >= 100"),
            Achievement("daily_1000", "一日千善", "单日获得1000次功德", "🔥", "todayCount >= 1000"),
            Achievement("streak_7", "七日修行", "连续7天使用", "📅", "streak >= 7"),
            Achievement("streak_30", "一月苦修", "连续30天使用", "🌟", "streak >= 30"),
        )
    }

    val allAchievements: List<Achievement> get() = ACHIEVEMENTS

    /** 从 JSON 字符串读取已解锁 ID 集合 */
    private fun getUnlockedIds(): MutableSet<String> {
        val json = prefs.getString(KEY_UNLOCKED_JSON, null) ?: return mutableSetOf()
        return try {
            val arr = JSONArray(json)
            val set = mutableSetOf<String>()
            for (i in 0 until arr.length()) set.add(arr.getString(i))
            set
        } catch (_: Exception) {
            mutableSetOf()
        }
    }

    /** 将解锁 ID 集合写入 JSON 字符串 */
    private fun saveUnlockedIds(ids: Set<String>) {
        val arr = JSONArray()
        ids.forEach { arr.put(it) }
        prefs.edit().putString(KEY_UNLOCKED_JSON, arr.toString()).apply()
    }

    fun isUnlocked(id: String): Boolean = id in getUnlockedIds()

    fun checkAndUnlock(totalCount: Int, todayCount: Int): List<Achievement> {
        val unlocked = getUnlockedIds()
        val currentStreak = prefs.getInt(KEY_STREAK, 0)
        val newlyUnlocked = mutableListOf<Achievement>()

        for (a in ACHIEVEMENTS) {
            if (a.id in unlocked) continue
            val met = when (a.id) {
                "first_merit" -> totalCount >= 1
                "merit_100" -> totalCount >= 100
                "merit_1000" -> totalCount >= 1000
                "merit_10000" -> totalCount >= 10000
                "daily_100" -> todayCount >= 100
                "daily_1000" -> todayCount >= 1000
                "streak_7" -> currentStreak >= 7
                "streak_30" -> currentStreak >= 30
                else -> false
            }
            if (met) {
                unlocked.add(a.id)
                newlyUnlocked.add(a)
            }
        }

        if (newlyUnlocked.isNotEmpty()) {
            saveUnlockedIds(unlocked)
        }
        return newlyUnlocked
    }

    fun updateStreak() {
        val today = LocalDate.now().format(DATE_FORMAT)
        val lastActive = prefs.getString(KEY_LAST_ACTIVE_DATE, null)
        if (lastActive == today) return

        val currentStreak = prefs.getInt(KEY_STREAK, 0)
        if (lastActive != null) {
            val daysBetween = ChronoUnit.DAYS.between(
                LocalDate.parse(lastActive, DATE_FORMAT), LocalDate.now()
            )
            val newStreak = if (daysBetween == 1L) currentStreak + 1 else 1
            prefs.edit()
                .putInt(KEY_STREAK, newStreak)
                .putString(KEY_LAST_ACTIVE_DATE, today)
                .apply()
        } else {
            prefs.edit()
                .putInt(KEY_STREAK, 1)
                .putString(KEY_LAST_ACTIVE_DATE, today)
                .apply()
        }
    }
}
