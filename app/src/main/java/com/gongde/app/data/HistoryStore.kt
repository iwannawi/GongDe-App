package com.gongde.app.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 功德历史记录存储管理器
 * 使用 SharedPreferences + JSON 格式存储每日功德数据
 * 支持记录、查询、统计等功能
 */
class HistoryStore(context: Context) {

    companion object {
        private const val PREFS_NAME = "history_prefs"
        private const val KEY_HISTORY_DATA = "history_data"
        private val DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // 内存缓存，避免每次操作都解析 JSON
    private var cache: MutableMap<String, Int>? = null

    /** 获取缓存，首次访问时从磁盘加载 */
    private fun getCache(): MutableMap<String, Int> {
        cache?.let { return it }
        val map = loadFromDisk()
        cache = map
        return map
    }

    /** 从磁盘加载 JSON */
    private fun loadFromDisk(): MutableMap<String, Int> {
        val jsonStr = prefs.getString(KEY_HISTORY_DATA, null) ?: return mutableMapOf()
        return try {
            val map = mutableMapOf<String, Int>()
            val jsonObject = JSONObject(jsonStr)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = jsonObject.optInt(key, 0)
            }
            map
        } catch (e: Exception) {
            Log.e("HistoryStore", "Failed to load history from disk", e)
            mutableMapOf()
        }
    }

    /** 将缓存写回磁盘 */
    private fun flushToDisk(map: Map<String, Int>) {
        try {
            val jsonObject = JSONObject()
            for ((date, count) in map) {
                jsonObject.put(date, count)
            }
            prefs.edit().putString(KEY_HISTORY_DATA, jsonObject.toString()).apply()
        } catch (e: Exception) {
            Log.e("HistoryStore", "Failed to flush history to disk", e)
        }
    }

    @Synchronized
    fun recordMerit(date: String, count: Int) {
        val map = getCache()
        map[date] = (map[date] ?: 0) + count
        flushToDisk(map)
    }

    /**
     * 获取所有历史记录
     * @return 日期→次数的映射，按日期降序排列（最新在前）
     */
    fun getHistory(): Map<String, Int> {
        return getCache().toSortedMap(compareByDescending { it })
    }

    fun getRecentDays(n: Int): List<Pair<String, Int>> {
        val map = getCache()
        val today = LocalDate.now()
        val result = mutableListOf<Pair<String, Int>>()
        for (i in 0 until n) {
            val date = today.minusDays(i.toLong()).format(DATE_FORMAT)
            result.add(date to (map[date] ?: 0))
        }
        return result
    }

    fun getWeekTotal(): Int = getTotalForDays(7)

    fun getMonthTotal(): Int = getTotalForDays(30)

    private fun getTotalForDays(days: Int): Int {
        val map = getCache()
        val today = LocalDate.now()
        var total = 0
        for (i in 0 until days) {
            val date = today.minusDays(i.toLong()).format(DATE_FORMAT)
            total += map[date] ?: 0
        }
        return total
    }

    /**
     * 清理超过指定天数的历史数据，防止无限增长
     * @param keepDays 保留最近多少天的数据，默认 90 天
     */
    @Synchronized
    fun cleanup(keepDays: Int = 90) {
        val map = getCache()
        val cutoff = LocalDate.now().minusDays(keepDays.toLong()).format(DATE_FORMAT)
        val iterator = map.iterator()
        var changed = false
        while (iterator.hasNext()) {
            if (iterator.next().key < cutoff) {
                iterator.remove()
                changed = true
            }
        }
        if (changed) flushToDisk(map)
    }
}
