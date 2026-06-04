package com.gongde.app.data

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MeritStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("merit_prefs", Context.MODE_PRIVATE)

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private var _totalCount: Int = prefs.getInt(KEY_TOTAL, 0)
    private var _todayCount: Int = -1
    private var _todayDate: String = prefs.getString(KEY_TODAY_DATE, "") ?: ""

    var totalCount: Int
        get() = _totalCount
        private set(value) {
            _totalCount = value
            prefs.edit().putInt(KEY_TOTAL, value).apply()
        }

    var todayCount: Int
        get() {
            val today = LocalDate.now().format(dateFormatter)
            if (_todayDate != today) {
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

    @Synchronized
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

    companion object {
        private const val KEY_TOTAL = "total_count"
        private const val KEY_TODAY = "today_count"
        private const val KEY_TODAY_DATE = "today_date"
    }
}
