package com.gongde.app.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gongde.app.data.AchievementStore
import com.gongde.app.data.HistoryStore
import com.gongde.app.data.MeritStore
import com.gongde.app.ui.SoundEngine
import com.gongde.app.ui.SwitchType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** 设置操作类型安全枚举 */
sealed class SettingsAction {
    data class SetHaptic(val enabled: Boolean) : SettingsAction()
    data class SetSwitchType(val type: String) : SettingsAction()
    data class SetTheme(val themeId: String) : SettingsAction()
    data class SetAsmr(val enabled: Boolean) : SettingsAction()
}

/** UI 状态数据类 */
data class MeritUiState(
    val totalCount: Int = 0,
    val todayCount: Int = 0,
    val triggerCount: Int = 0,
    val showResetDialog: Boolean = false,
    val hapticEnabled: Boolean = true,
    val switchType: SwitchType = SwitchType.BLUE,
    val asmrEnabled: Boolean = false,
    val themeId: String = "deep_purple"
)

class GongDeViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application
    private val store = MeritStore(context)
    val achievementStore = AchievementStore(context)
    val historyStore = HistoryStore(context)
    val soundEngine = SoundEngine()

    private var _uiState = MeritUiState(
        totalCount = store.totalCount,
        todayCount = store.todayCount,
        hapticEnabled = store.hapticEnabled,
        switchType = try { SwitchType.valueOf(store.switchType.uppercase()) } catch (_: Exception) { SwitchType.BLUE },
        asmrEnabled = store.asmrEnabled,
        themeId = store.themeId
    )
    val uiState: MeritUiState get() = _uiState

    fun incrementMerit() {
        try {
            val (newTotal, newToday) = store.increment()
            _uiState = _uiState.copy(
                totalCount = newTotal,
                todayCount = newToday,
                triggerCount = _uiState.triggerCount + 1
            )
            viewModelScope.launch(Dispatchers.IO) {
                historyStore.recordMerit(LocalDate.now().toString(), 1)
                achievementStore.updateStreak()
                val unlocked = achievementStore.checkAndUnlock(newTotal, newToday)
                if (unlocked.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        for (a in unlocked) {
                            Toast.makeText(context, "🏆 成就解锁：${a.name}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } catch (_: Exception) { }
    }

    fun resetMerit() {
        store.reset()
        _uiState = _uiState.copy(totalCount = 0, todayCount = 0, showResetDialog = false)
    }

    fun showDialog(show: Boolean) {
        _uiState = _uiState.copy(showResetDialog = show)
    }

    fun handleSettings(action: SettingsAction) {
        when (action) {
            is SettingsAction.SetHaptic -> {
                _uiState = _uiState.copy(hapticEnabled = action.enabled)
                store.hapticEnabled = action.enabled
            }
            is SettingsAction.SetSwitchType -> {
                val type = try { SwitchType.valueOf(action.type.uppercase()) } catch (_: Exception) { SwitchType.BLUE }
                _uiState = _uiState.copy(switchType = type)
                store.switchType = action.type
            }
            is SettingsAction.SetTheme -> {
                _uiState = _uiState.copy(themeId = action.themeId)
                store.themeId = action.themeId
            }
            is SettingsAction.SetAsmr -> {
                _uiState = _uiState.copy(asmrEnabled = action.enabled)
                store.asmrEnabled = action.enabled
            }
        }
    }

    init {
        // 启动预热 + 清理历史
        soundEngine.warmUp(_uiState.switchType)
        viewModelScope.launch(Dispatchers.IO) { historyStore.cleanup() }
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.release()
    }
}
