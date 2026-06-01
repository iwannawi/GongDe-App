/**
 * 功德数据持久化存储
 *
 * 使用 SharedPreferences 保存功德计数数据，包括：
 * - 累计功德总数（跨日期保留）
 * - 今日功德计数（按日期自动重置）
 * - 记录日期以判断是否需要重置当日计数
 */

package com.gongde.app.data

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 功德数据存储管理类
 *
 * 通过 SharedPreferences 实现功德计数的本地持久化，
 * 支持累计计数和按日期自动重置的今日计数。
 *
 * @param context Android 上下文，用于获取 SharedPreferences 实例
 */
class MeritStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("merit_prefs", Context.MODE_PRIVATE)

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    // 内存缓存，避免每次 getter 都读磁盘
    private var _totalCount: Int = prefs.getInt(KEY_TOTAL, 0)
    private var _todayCount: Int = -1  // -1 表示未初始化
    private var _todayDate: String = prefs.getString(KEY_TODAY_DATE, "") ?: ""

    /**
     * 累计功德总数（带内存缓存）
     */
    var totalCount: Int
        get() = _totalCount
        private set(value) {
            _totalCount = value
            prefs.edit().putInt(KEY_TOTAL, value).apply()
        }

    /**
     * 今日功德计数（带内存缓存，自动处理跨天重置）
     */
    var todayCount: Int
        get() {
            val today = LocalDate.now().format(dateFormatter)
            if (_todayDate != today) {
                // 跨天，重置
                _todayDate = today
                _todayCount = 0
                prefs.edit()
                    .putString(KEY_TODAY_DATE, today)
                    .putInt(KEY_TODAY, 0)
                    .apply()
            }
            if (_todayCount < 0) {
                _todayCount = prefs.getInt(KEY_TODAY, 0)
            }
            return _todayCount
        }
        private set(value) {
            _todayCount = value
            _todayDate = LocalDate.now().format(dateFormatter)
            prefs.edit()
                .putString(KEY_TODAY_DATE, _todayDate)
                .putInt(KEY_TODAY, value)
                .apply()
        }

    // ==================== 设置项 ====================

    /** 触觉反馈开关（默认开启） */
    var hapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC, value).apply()

    /** 当前机械轴类型："blue"=青轴 / "red"=红轴 / "brown"=茶轴 */
    var switchType: String
        get() = prefs.getString(KEY_SWITCH, "blue") ?: "blue"
        set(value) = prefs.edit().putString(KEY_SWITCH, value).apply()

    /** 当前主题 ID：deep_purple / cyber_blue / emerald / flame */
    var themeId: String
        get() = prefs.getString(KEY_THEME, "deep_purple") ?: "deep_purple"
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    /** ASMR 模式开关 */
    var asmrEnabled: Boolean
        get() = prefs.getBoolean(KEY_ASMR, false)
        set(value) = prefs.edit().putBoolean(KEY_ASMR, value).apply()

    /** 最近活跃日期（用于连续天数计算） */
    var lastActiveDate: String
        get() = prefs.getString(KEY_LAST_ACTIVE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_ACTIVE, value).apply()

    /** 连续活跃天数 */
    var streak: Int
        get() = prefs.getInt(KEY_STREAK, 0)
        set(value) = prefs.edit().putInt(KEY_STREAK, value).apply()

    // ==================== 核心方法 ====================

    /**
     * 重置所有功德计数
     *
     * 将累计功德和今日功德都清零，并更新日期标记为今天。
     */
    fun reset() {
        _totalCount = 0
        _todayCount = 0
        _todayDate = LocalDate.now().format(dateFormatter)
        prefs.edit()
            .putInt(KEY_TOTAL, 0)
            .putInt(KEY_TODAY, 0)
            .putString(KEY_TODAY_DATE, _todayDate)
            .apply()
    }

    /**
     * 功德递增操作（核心方法）
     *
     * 同时递增累计功德和今日功德，并一次性批量写入持久化存储。
     * 自动处理跨天日期判断：如果今日日期已变化，今日计数从 0 开始。
     *
     * @return 包含新的(累计功德, 今日功德)的 Pair
     */
    fun increment(): Pair<Int, Int> {
        val today = LocalDate.now().format(dateFormatter)
        val currentTotal = _totalCount
        val currentToday = if (_todayDate == today && _todayCount >= 0) {
            _todayCount
        } else {
            0
        }
        val newTotal = currentTotal + 1
        val newToday = currentToday + 1
        _totalCount = newTotal
        _todayCount = newToday
        _todayDate = today
        prefs.edit()
            .putInt(KEY_TOTAL, newTotal)
            .putString(KEY_TODAY_DATE, today)
            .putInt(KEY_TODAY, newToday)
            .apply()
        return Pair(newTotal, newToday)
    }

    /**
     * SharedPreferences 键名常量
     */
    companion object {
        private const val KEY_TOTAL = "total_count"
        private const val KEY_TODAY = "today_count"
        private const val KEY_TODAY_DATE = "today_date"
        private const val KEY_HAPTIC = "haptic_enabled"
        private const val KEY_SWITCH = "switch_type"
        private const val KEY_THEME = "theme_id"
        private const val KEY_ASMR = "asmr_enabled"
        private const val KEY_LAST_ACTIVE = "last_active_date"
        private const val KEY_STREAK = "streak"
    }
}
