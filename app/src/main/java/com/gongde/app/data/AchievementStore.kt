package com.gongde.app.data

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 成就数据类
 * @param id 成就唯一标识
 * @param name 中文名称
 * @param description 中文描述
 * @param icon Emoji图标
 * @param condition 人类可读的解锁条件
 */
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val condition: String,
)

/**
 * 成就存储管理器
 * 使用 SharedPreferences 持久化已解锁成就和连续打卡天数
 */
class AchievementStore(context: Context) {

    companion object {
        /** SharedPreferences 文件名 */
        private const val PREFS_NAME = "achievement_prefs"

        /** 已解锁成就 ID 集合的 key */
        private const val KEY_UNLOCKED_IDS = "unlocked_ids"

        /** 上次活跃日期的 key */
        private const val KEY_LAST_ACTIVE_DATE = "last_active_date"

        /** 当前连续打卡天数的 key */
        private const val KEY_STREAK = "current_streak"

        /** 日期格式化器，用于日期比较 */
        private val DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE

        /**
         * 预定义成就列表
         * 每个成就包含：id、中文名称、中文描述、Emoji图标、解锁条件
         */
        val ACHIEVEMENTS: List<Achievement> = listOf(
            Achievement(
                id = "first_merit",
                name = "初入佛门",
                description = "累计获得1次功德",
                icon = "🌱",
                condition = "totalCount >= 1"
            ),
            Achievement(
                id = "merit_100",
                name = "功德小成",
                description = "累计获得100次功德",
                icon = "🏅",
                condition = "totalCount >= 100"
            ),
            Achievement(
                id = "merit_1000",
                name = "功德精进",
                description = "累计获得1000次功德",
                icon = "🏆",
                condition = "totalCount >= 1000"
            ),
            Achievement(
                id = "merit_10000",
                name = "功德无量",
                description = "累计获得10000次功德",
                icon = "👑",
                condition = "totalCount >= 10000"
            ),
            Achievement(
                id = "daily_100",
                name = "一日百善",
                description = "单日获得100次功德",
                icon = "⚡",
                condition = "todayCount >= 100"
            ),
            Achievement(
                id = "daily_1000",
                name = "一日千善",
                description = "单日获得1000次功德",
                icon = "🔥",
                condition = "todayCount >= 1000"
            ),
            Achievement(
                id = "streak_7",
                name = "七日修行",
                description = "连续7天使用",
                icon = "📅",
                condition = "streak >= 7"
            ),
            Achievement(
                id = "streak_30",
                name = "一月苦修",
                description = "连续30天使用",
                icon = "🌟",
                condition = "streak >= 30"
            ),
        )
    }

    /** SharedPreferences 实例 */
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * 获取所有预定义成就列表
     */
    val allAchievements: List<Achievement>
        get() = ACHIEVEMENTS

    /**
     * 获取已解锁的成就 ID 集合
     */
    val unlockedIds: Set<String>
        get() = prefs.getStringSet(KEY_UNLOCKED_IDS, emptySet()) ?: emptySet()

    /**
     * 判断指定成就是否已解锁
     * @param id 成就 ID
     * @return 是否已解锁
     */
    fun isUnlocked(id: String): Boolean {
        return id in unlockedIds
    }

    /**
     * 检查并解锁满足条件的成就
     * 遍历所有预定义成就，判断条件是否满足，若满足且未解锁则自动解锁
     *
     * @param totalCount 累计功德总数
     * @param todayCount 今日功德数
     * @return 本次新解锁的成就列表
     */
    fun checkAndUnlock(totalCount: Int, todayCount: Int): List<Achievement> {
        val currentUnlocked = unlockedIds.toMutableSet()
        val currentStreak = prefs.getInt(KEY_STREAK, 0)
        val newlyUnlocked = mutableListOf<Achievement>()

        for (achievement in ACHIEVEMENTS) {
            // 跳过已解锁的成就
            if (achievement.id in currentUnlocked) continue

            // 根据成就 ID 判断对应的条件
            val isMet = when (achievement.id) {
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

            // 条件满足且未解锁，加入新解锁列表
            if (isMet) {
                currentUnlocked.add(achievement.id)
                newlyUnlocked.add(achievement)
            }
        }

        // 将更新后的解锁集合写回 SharedPreferences
        if (newlyUnlocked.isNotEmpty()) {
            prefs.edit().putStringSet(KEY_UNLOCKED_IDS, currentUnlocked).commit()
        }

        return newlyUnlocked
    }

    /**
     * 更新连续打卡天数
     * 由每日启动时调用，逻辑如下：
     * - 若今天是首次打开，检查上次活跃日期
     * - 若上次活跃是昨天，则连续天数 +1
     * - 若上次活跃是今天，则不做变化
     * - 若间隔超过1天，则重置连续天数为1
     */
    fun updateStreak() {
        val today = LocalDate.now().format(DATE_FORMAT)
        val lastActive = prefs.getString(KEY_LAST_ACTIVE_DATE, null)

        // 今天已经更新过，无需重复处理
        if (lastActive == today) return

        val currentStreak = prefs.getInt(KEY_STREAK, 0)

        if (lastActive != null) {
            // 计算上次活跃日期与今天的间隔天数
            val lastDate = LocalDate.parse(lastActive, DATE_FORMAT)
            val daysBetween = ChronoUnit.DAYS.between(lastDate, LocalDate.now())

            if (daysBetween == 1L) {
                // 昨天活跃，连续天数 +1
                prefs.edit()
                    .putInt(KEY_STREAK, currentStreak + 1)
                    .putString(KEY_LAST_ACTIVE_DATE, today)
                    .apply()
            } else {
                // 间隔超过1天，重置连续天数为1
                prefs.edit()
                    .putInt(KEY_STREAK, 1)
                    .putString(KEY_LAST_ACTIVE_DATE, today)
                    .apply()
            }
        } else {
            // 首次使用，连续天数初始化为1
            prefs.edit()
                .putInt(KEY_STREAK, 1)
                .putString(KEY_LAST_ACTIVE_DATE, today)
                .apply()
        }
    }
}
