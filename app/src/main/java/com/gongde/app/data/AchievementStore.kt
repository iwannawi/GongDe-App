package com.gongde.app.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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

    // 内存缓存，避免每次调用都解析 JSON
    private var _unlockedCache: MutableSet<String>? = null
    private var _streakCache: Int? = null

    companion object {
        private const val KEY_UNLOCKED_JSON = "unlocked_json"
        private const val KEY_LAST_ACTIVE_DATE = "last_active_date"
        private const val KEY_STREAK = "current_streak"
        private val DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE

        val ACHIEVEMENTS: List<Achievement> = listOf(
            Achievement("first_merit", "新手上路", "累计获得1次功德", "🌱", "totalCount >= 1"),
            Achievement("merit_100", "小有成就", "累计获得100次功德", "🏅", "totalCount >= 100"),
            Achievement("merit_1000", "手速达人", "累计获得1000次功德", "🏆", "totalCount >= 1000"),
            Achievement("merit_10000", "功德无量", "累计获得10000次功德", "👑", "totalCount >= 10000"),
            Achievement("daily_100", "今日内卷", "单日获得100次功德", "⚡", "todayCount >= 100"),
            Achievement("daily_1000", "千击不倦", "单日获得1000次功德", "🔥", "todayCount >= 1000"),
            Achievement("streak_7", "周周不落", "连续7天使用", "📅", "streak >= 7"),
            Achievement("streak_30", "月度劳模", "连续30天使用", "🌟", "streak >= 30"),
        )
    }

    val allAchievements: List<Achievement> get() = ACHIEVEMENTS

    /** 从 JSON 字符串读取已解锁 ID 集合（带内存缓存） */
    private fun getUnlockedIds(): MutableSet<String> {
        _unlockedCache?.let { return it }
        val json = prefs.getString(KEY_UNLOCKED_JSON, null) ?: return mutableSetOf<String>().also { _unlockedCache = it }
        return try {
            val arr = JSONArray(json)
            val set = mutableSetOf<String>()
            for (i in 0 until arr.length()) set.add(arr.getString(i))
            set.also { _unlockedCache = it }
        } catch (e: Exception) {
            Log.e("AchievementStore", "Failed to parse unlocked IDs", e)
            mutableSetOf<String>().also { _unlockedCache = it }
        }
    }

    /** 将解锁 ID 集合写入 JSON 字符串 */
    private fun saveUnlockedIds(ids: Set<String>) {
        val arr = JSONArray()
        ids.forEach { arr.put(it) }
        prefs.edit().putString(KEY_UNLOCKED_JSON, arr.toString()).apply()
        _unlockedCache = ids.toMutableSet()
    }

    fun isUnlocked(id: String): Boolean = id in getUnlockedIds()

    fun checkAndUnlock(totalCount: Int, todayCount: Int): List<Achievement> {
        val unlocked = getUnlockedIds()
        val currentStreak = _streakCache ?: prefs.getInt(KEY_STREAK, 0).also { _streakCache = it }
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

        val currentStreak = _streakCache ?: prefs.getInt(KEY_STREAK, 0)
        if (lastActive != null) {
            val daysBetween = ChronoUnit.DAYS.between(
                LocalDate.parse(lastActive, DATE_FORMAT), LocalDate.now()
            )
            val newStreak = if (daysBetween == 1L) currentStreak + 1 else 1
            _streakCache = newStreak
            prefs.edit()
                .putInt(KEY_STREAK, newStreak)
                .putString(KEY_LAST_ACTIVE_DATE, today)
                .apply()
        } else {
            _streakCache = 1
            prefs.edit()
                .putInt(KEY_STREAK, 1)
                .putString(KEY_LAST_ACTIVE_DATE, today)
                .apply()
        }
    }
}
