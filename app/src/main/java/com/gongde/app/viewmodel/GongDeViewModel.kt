package com.gongde.app.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gongde.app.data.GongDeRepository
import com.gongde.app.ui.SoundEngine
import com.gongde.app.ui.SwitchType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** 设置操作类型安全枚举 */
sealed class SettingsAction {
    data class SetHaptic(val enabled: Boolean) : SettingsAction()
    data class SetSwitchType(val type: String) : SettingsAction()
    data class SetTheme(val themeId: String) : SettingsAction()
}

/** UI 状态数据类 */
data class MeritUiState(
    val totalCount: Int = 0,
    val todayCount: Int = 0,
    val triggerCount: Int = 0,
    val showResetDialog: Boolean = false,
    val hapticEnabled: Boolean = true,
    val switchType: SwitchType = SwitchType.BLUE,
    val themeId: String = "deep_purple",
    val weekTotal: Int = 0,
    val monthTotal: Int = 0
)

class GongDeViewModel(
    private val repo: GongDeRepository,
    private val appContext: android.content.Context,
    val historyStore: com.gongde.app.data.HistoryStore,
    val achievementStore: com.gongde.app.data.AchievementStore
) : ViewModel() {

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val meritStore = com.gongde.app.data.MeritStore(app)
            val achievementStore = com.gongde.app.data.AchievementStore(app)
            val prefsStore = com.gongde.app.data.PreferencesStore(app)
            val historyStore = com.gongde.app.data.HistoryStore(app)
            val historyDao = com.gongde.app.data.AppDatabase.create(app).historyDao()
            val repo = com.gongde.app.data.GongDeRepository(meritStore, historyDao, prefsStore, achievementStore)
            return GongDeViewModel(repo, app, historyStore, achievementStore) as T
        }
    }

    val soundEngine = SoundEngine()
    private val achievementMutex = Mutex()

    private var _uiState by mutableStateOf(MeritUiState())
    val uiState: MeritUiState get() = _uiState

    init {
        // 初始化 UI 状态（从 DataStore + MeritStore 读取）
        viewModelScope.launch(Dispatchers.IO) {
            val haptic = repo.getHapticEnabled()
            val switch = repo.getSwitchType()
            val theme = repo.getThemeId()
            val switchType = try { SwitchType.valueOf(switch.uppercase()) } catch (_: Exception) { SwitchType.BLUE }
            val week = repo.getWeekTotal()
            val month = repo.getMonthTotal()

            withContext(Dispatchers.Main) {
                _uiState = _uiState.copy(
                    totalCount = repo.totalCount,
                    todayCount = repo.todayCount,
                    hapticEnabled = haptic,
                    switchType = switchType,
                    themeId = theme,
                    weekTotal = week,
                    monthTotal = month
                )
            }
        }

        soundEngine.warmUp()
        viewModelScope.launch(Dispatchers.IO) { repo.cleanupOldHistory() }
    }

    fun incrementMerit() {
        try {
            val (newTotal, newToday) = repo.incrementMerit()
            _uiState = _uiState.copy(
                totalCount = newTotal,
                todayCount = newToday,
                triggerCount = _uiState.triggerCount + 1
            )
            viewModelScope.launch(Dispatchers.IO) {
                repo.recordMerit(1)
                repo.updateStreak()
                achievementMutex.lock()
                try {
                    val unlocked = repo.checkAndUnlock(newTotal, newToday)
                    if (unlocked.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            for (a in unlocked) {
                                Toast.makeText(appContext, "🏆 成就解锁：${a.name}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } finally {
                    achievementMutex.unlock()
                }
                refreshStats()
            }
        } catch (e: Exception) {
            Log.e("GongDeVM", "Failed to increment merit", e)
        }
    }

    fun resetMerit() {
        repo.resetMerit()
        _uiState = _uiState.copy(totalCount = 0, todayCount = 0, showResetDialog = false)
        viewModelScope.launch(Dispatchers.IO) { refreshStats() }
    }

    fun showDialog(show: Boolean) {
        _uiState = _uiState.copy(showResetDialog = show)
    }

    /** 从 store 同步最新计数到 UI state（专注/ASMR 直接操作 store 后调用） */
    fun syncFromStore() {
        val snapTotal = repo.totalCount
        val snapToday = repo.todayCount
        _uiState = _uiState.copy(totalCount = snapTotal, todayCount = snapToday)
        viewModelScope.launch(Dispatchers.IO) {
            repo.checkAndUnlock(snapTotal, snapToday)
            refreshStats()
        }
    }

    /** 专注/ASMR 专用：递增 store + 记录历史，但不触发浮动文字 */
    fun incrementStore() {
        repo.incrementMerit()
        viewModelScope.launch(Dispatchers.IO) { repo.recordMerit(1) }
    }

    fun handleSettings(action: SettingsAction) {
        when (action) {
            is SettingsAction.SetHaptic -> {
                _uiState = _uiState.copy(hapticEnabled = action.enabled)
                viewModelScope.launch(Dispatchers.IO) { repo.setHapticEnabled(action.enabled) }
            }
            is SettingsAction.SetSwitchType -> {
                val type = try { SwitchType.valueOf(action.type.uppercase()) } catch (_: Exception) { SwitchType.BLUE }
                _uiState = _uiState.copy(switchType = type)
                viewModelScope.launch(Dispatchers.IO) { repo.setSwitchType(action.type) }
            }
            is SettingsAction.SetTheme -> {
                _uiState = _uiState.copy(themeId = action.themeId)
                viewModelScope.launch(Dispatchers.IO) { repo.setThemeId(action.themeId) }
            }
        }
    }

    private suspend fun refreshStats() {
        val week = repo.getWeekTotal()
        val month = repo.getMonthTotal()
        withContext(Dispatchers.Main) {
            _uiState = _uiState.copy(weekTotal = week, monthTotal = month)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.release()
    }
}
