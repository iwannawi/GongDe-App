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
}
