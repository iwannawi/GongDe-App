package com.gongde.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * 基于 DataStore 的用户偏好存储
 * 替代原 MeritStore 中的设置项，保证类型安全和原子性
 */
class PreferencesStore(private val context: Context) {

    private object Keys {
        val HAPTIC = booleanPreferencesKey("haptic_enabled")
        val SWITCH_TYPE = stringPreferencesKey("switch_type")
        val THEME_ID = stringPreferencesKey("theme_id")
        val LAST_ACTIVE_DATE = stringPreferencesKey("last_active_date")
        val STREAK = intPreferencesKey("current_streak")
        val FIRST_PRESS_COMPLETED = booleanPreferencesKey("first_press_completed")
        val DAILY_GOAL_COMPLETED_DATE = stringPreferencesKey("daily_goal_completed_date")
        val ROUNDS_TODAY_DATE = stringPreferencesKey("rounds_today_date")
        val ROUNDS_TODAY = intPreferencesKey("rounds_today")
        val BEST_COMBO_TODAY_DATE = stringPreferencesKey("best_combo_today_date")
        val BEST_COMBO_TODAY = intPreferencesKey("best_combo_today")
        val BEST_COMBO_ALL_TIME = intPreferencesKey("best_combo_all_time")
        val EMOTION_CARD_INDEX = intPreferencesKey("emotion_card_index")
        val DAILY_REWARD_CLAIMED_DATE = stringPreferencesKey("daily_reward_claimed_date")
    }

    // --- Flow 观察 ---
    val hapticEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.HAPTIC] ?: true }
    val switchType: Flow<String> = context.dataStore.data.map { it[Keys.SWITCH_TYPE] ?: "blue" }
    val themeId: Flow<String> = context.dataStore.data.map { it[Keys.THEME_ID] ?: "morning_mist" }

    // --- 同步读取（用于 ViewModel 初始化） ---
    suspend fun getHapticEnabled(): Boolean = context.dataStore.data.first()[Keys.HAPTIC] ?: true
    suspend fun getSwitchType(): String = context.dataStore.data.first()[Keys.SWITCH_TYPE] ?: "blue"
    suspend fun getThemeId(): String = context.dataStore.data.first()[Keys.THEME_ID] ?: "morning_mist"
    suspend fun getLastActiveDate(): String = context.dataStore.data.first()[Keys.LAST_ACTIVE_DATE] ?: ""
    suspend fun getStreak(): Int = context.dataStore.data.first()[Keys.STREAK] ?: 0
    suspend fun getFirstPressCompleted(): Boolean =
        context.dataStore.data.first()[Keys.FIRST_PRESS_COMPLETED] ?: false
    suspend fun getDailyGoalCompletedDate(): String =
        context.dataStore.data.first()[Keys.DAILY_GOAL_COMPLETED_DATE] ?: ""
    suspend fun getRoundsToday(): Int {
        val prefs = context.dataStore.data.first()
        return if (prefs[Keys.ROUNDS_TODAY_DATE] == LocalDate.now().toString()) {
            prefs[Keys.ROUNDS_TODAY] ?: 0
        } else {
            0
        }
    }
    suspend fun getBestComboToday(): Int {
        val prefs = context.dataStore.data.first()
        return if (prefs[Keys.BEST_COMBO_TODAY_DATE] == LocalDate.now().toString()) {
            prefs[Keys.BEST_COMBO_TODAY] ?: 0
        } else {
            0
        }
    }
    suspend fun getBestComboAllTime(): Int = context.dataStore.data.first()[Keys.BEST_COMBO_ALL_TIME] ?: 0
    suspend fun getEmotionCardIndex(): Int = context.dataStore.data.first()[Keys.EMOTION_CARD_INDEX] ?: 0
    suspend fun getDailyRewardClaimedDate(): String =
        context.dataStore.data.first()[Keys.DAILY_REWARD_CLAIMED_DATE] ?: ""

    // --- 写入 ---
    suspend fun setHapticEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.HAPTIC] = value }
    }

    suspend fun setSwitchType(value: String) {
        context.dataStore.edit { it[Keys.SWITCH_TYPE] = value }
    }

    suspend fun setThemeId(value: String) {
        context.dataStore.edit { it[Keys.THEME_ID] = value }
    }

    suspend fun setLastActiveDate(value: String) {
        context.dataStore.edit { it[Keys.LAST_ACTIVE_DATE] = value }
    }

    suspend fun setStreak(value: Int) {
        context.dataStore.edit { it[Keys.STREAK] = value }
    }

    suspend fun setFirstPressCompleted(value: Boolean) {
        context.dataStore.edit { it[Keys.FIRST_PRESS_COMPLETED] = value }
    }

    suspend fun setDailyGoalCompletedDate(value: String) {
        context.dataStore.edit { it[Keys.DAILY_GOAL_COMPLETED_DATE] = value }
    }

    suspend fun setEmotionCardIndex(value: Int) {
        context.dataStore.edit { it[Keys.EMOTION_CARD_INDEX] = value }
    }

    suspend fun setDailyRewardClaimedDate(value: String) {
        context.dataStore.edit { it[Keys.DAILY_REWARD_CLAIMED_DATE] = value }
    }

    suspend fun recordReliefRound(bestCombo: Int) {
        val today = LocalDate.now().toString()
        context.dataStore.edit { prefs ->
            val currentRounds = if (prefs[Keys.ROUNDS_TODAY_DATE] == today) prefs[Keys.ROUNDS_TODAY] ?: 0 else 0
            val currentBestToday = if (prefs[Keys.BEST_COMBO_TODAY_DATE] == today) prefs[Keys.BEST_COMBO_TODAY] ?: 0 else 0
            val currentBestAll = prefs[Keys.BEST_COMBO_ALL_TIME] ?: 0
            prefs[Keys.ROUNDS_TODAY_DATE] = today
            prefs[Keys.ROUNDS_TODAY] = currentRounds + 1
            prefs[Keys.BEST_COMBO_TODAY_DATE] = today
            prefs[Keys.BEST_COMBO_TODAY] = maxOf(currentBestToday, bestCombo)
            prefs[Keys.BEST_COMBO_ALL_TIME] = maxOf(currentBestAll, bestCombo)
        }
    }
}
