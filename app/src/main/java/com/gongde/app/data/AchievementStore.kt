package com.gongde.app.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.time.LocalDate
import org.json.JSONArray

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val metric: AchievementMetric,
    val target: Int,
)

enum class AchievementMetric { TOTAL, TODAY, STREAK }

class AchievementStore(
    context: Context,
    private val currentDate: () -> LocalDate = { LocalDate.now() }
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("achievement_prefs", Context.MODE_PRIVATE)

    private var _unlockedCache: MutableSet<String>? = null

    companion object {
        private const val KEY_UNLOCKED_JSON = "unlocked_json"
        private const val KEY_DAILY_UNLOCK_PREFIX = "daily_unlock_"

        val DAILY_ACHIEVEMENT_IDS: Set<String> = setOf("daily_100", "daily_1000")

        val ACHIEVEMENTS: List<Achievement> = listOf(
            Achievement("first_merit", "新手上路", "累计获得 1 次功德", "🌱", AchievementMetric.TOTAL, 1),
            Achievement("merit_1000", "手速达人", "累计获得 1000 次功德", "🏆", AchievementMetric.TOTAL, 1000),
            Achievement("daily_100", "今日内卷", "单日获得 100 次功德", "⚡", AchievementMetric.TODAY, 100),
            Achievement("daily_1000", "千击不倦", "单日获得 1000 次功德", "🔥", AchievementMetric.TODAY, 1000),
            Achievement("streak_7", "周周不落", "连续使用 7 天", "📅", AchievementMetric.STREAK, 7),
            Achievement("streak_30", "月度劳模", "连续使用 30 天", "🌟", AchievementMetric.STREAK, 30),
        )
    }

    val allAchievements: List<Achievement> get() = ACHIEVEMENTS

    private fun getUnlockedIds(): MutableSet<String> {
        _unlockedCache?.let { return it }
        val json = prefs.getString(KEY_UNLOCKED_JSON, null) ?: return mutableSetOf<String>().also { _unlockedCache = it }
        return try {
            val arr = JSONArray(json)
            val set = mutableSetOf<String>()
            for (i in 0 until arr.length()) set.add(arr.getString(i))
            // 旧版本把每日成就永久保存；迁移后每日状态只由当天计数决定。
            if (set.removeAll(DAILY_ACHIEVEMENT_IDS)) {
                persistUnlockedIds(set)
            }
            set.also { _unlockedCache = it }
        } catch (e: Exception) {
            Log.e("AchievementStore", "Failed to parse unlocked IDs", e)
            mutableSetOf<String>().also { _unlockedCache = it }
        }
    }

    private fun saveUnlockedIds(ids: Set<String>) {
        persistUnlockedIds(ids)
        _unlockedCache = ids.toMutableSet()
    }

    private fun persistUnlockedIds(ids: Set<String>) {
        val arr = JSONArray()
        ids.forEach { arr.put(it) }
        prefs.edit().putString(KEY_UNLOCKED_JSON, arr.toString()).apply()
    }

    fun isUnlocked(id: String): Boolean = id in getUnlockedIds()

    fun checkAndUnlock(totalCount: Int, todayCount: Int, streak: Int): List<Achievement> {
        val unlocked = getUnlockedIds()
        val newlyUnlocked = mutableListOf<Achievement>()
        val today = currentDate().toString()
        val dailyUnlocks = mutableListOf<String>()

        for (a in ACHIEVEMENTS) {
            if (currentValue(a, totalCount, todayCount, streak) < a.target) continue
            if (a.id in DAILY_ACHIEVEMENT_IDS) {
                if (prefs.getString(KEY_DAILY_UNLOCK_PREFIX + a.id, null) != today) {
                    dailyUnlocks.add(a.id)
                    newlyUnlocked.add(a)
                }
            } else if (a.id !in unlocked) {
                unlocked.add(a.id)
                newlyUnlocked.add(a)
            }
        }

        if (newlyUnlocked.any { it.id !in DAILY_ACHIEVEMENT_IDS }) {
            saveUnlockedIds(unlocked)
        }
        if (dailyUnlocks.isNotEmpty()) {
            prefs.edit().apply {
                dailyUnlocks.forEach { putString(KEY_DAILY_UNLOCK_PREFIX + it, today) }
            }.apply()
        }
        return newlyUnlocked
    }

    fun getCompletedIds(totalCount: Int, todayCount: Int, streak: Int): Set<String> =
        ACHIEVEMENTS.asSequence()
            .filter { achievement ->
                achievement.id in getUnlockedIds() ||
                    currentValue(achievement, totalCount, todayCount, streak) >= achievement.target
            }
            .mapTo(mutableSetOf()) { it.id }

    fun currentValue(achievement: Achievement, totalCount: Int, todayCount: Int, streak: Int): Int =
        when (achievement.metric) {
            AchievementMetric.TOTAL -> totalCount
            AchievementMetric.TODAY -> todayCount
            AchievementMetric.STREAK -> streak
        }
}
