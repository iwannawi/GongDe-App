package com.gongde.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gongde.app.data.Achievement
import com.gongde.app.data.AchievementStore
import com.gongde.app.data.AppDatabase
import com.gongde.app.data.GongDeRepository
import com.gongde.app.data.MeritStore
import com.gongde.app.data.PreferencesStore
import com.gongde.app.ui.SoundEngine
import com.gongde.app.ui.SwitchType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

sealed class SettingsAction {
    data class SetHaptic(val enabled: Boolean) : SettingsAction()
    data class SetSwitchType(val type: String) : SettingsAction()
    data class SetTheme(val themeId: String) : SettingsAction()
}

data class MeritUiState(
    val totalCount: Int = 0,
    val todayCount: Int = 0,
    val triggerCount: Int = 0,
    val showResetDialog: Boolean = false,
    val hapticEnabled: Boolean = true,
    val switchType: SwitchType = SwitchType.BLUE,
    val themeId: String = "morning_mist",
    val weekTotal: Int = 0,
    val monthTotal: Int = 0,
    val recentDays: List<Pair<String, Int>> = emptyList(),
    val toastEvents: List<String> = emptyList()
)

class GongDeViewModel(
    private val repo: GongDeRepository,
    val achievementStore: AchievementStore
) : ViewModel() {

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val meritStore = MeritStore(app)
            val achievementStore = AchievementStore(app)
            val prefsStore = PreferencesStore(app)
            val historyDao = AppDatabase.getInstance(app).historyDao()
            val repo = GongDeRepository(meritStore, historyDao, prefsStore, achievementStore)
            return GongDeViewModel(repo, achievementStore) as T
        }
    }

    val soundEngine = SoundEngine()
    private val achievementMutex = Mutex()

    private var _uiState by mutableStateOf(MeritUiState())
    val uiState: MeritUiState get() = _uiState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val haptic = repo.getHapticEnabled()
            val switch = repo.getSwitchType()
            val theme = repo.getThemeId()
            val switchType = try { SwitchType.valueOf(switch.uppercase()) } catch (_: Exception) { SwitchType.BLUE }
            val week = repo.getWeekTotal()
            val month = repo.getMonthTotal()
            val days = repo.getRecentDays(30)

            withContext(Dispatchers.Main) {
                _uiState = _uiState.copy(
                    totalCount = repo.totalCount,
                    todayCount = repo.todayCount,
                    hapticEnabled = haptic,
                    switchType = switchType,
                    themeId = theme,
                    weekTotal = week,
                    monthTotal = month,
                    recentDays = days
                )
            }
        }

        soundEngine.warmUp()
        viewModelScope.launch(Dispatchers.IO) { repo.cleanupOldHistory() }
    }

    fun incrementMerit() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (newTotal, newToday) = repo.incrementMerit()
                withContext(Dispatchers.Main) {
                    _uiState = _uiState.copy(
                        totalCount = newTotal,
                        todayCount = newToday,
                        triggerCount = _uiState.triggerCount + 1
                    )
                }
                repo.recordMerit(1)
                repo.updateStreak()
                achievementMutex.lock()
                try {
                    val unlocked = repo.checkAndUnlock(newTotal, newToday)
                    if (unlocked.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            val messages = unlocked.map { a -> "🏆 成就解锁：${a.name}" }
                            _uiState = _uiState.copy(toastEvents = _uiState.toastEvents + messages)
                        }
                    }
                } finally {
                    achievementMutex.unlock()
                }
                refreshStats()
            } catch (e: Exception) {
                Log.e("GongDeVM", "Failed to increment merit", e)
            }
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

    fun syncFromStore() {
        val snapTotal = repo.totalCount
        val snapToday = repo.todayCount
        _uiState = _uiState.copy(totalCount = snapTotal, todayCount = snapToday)
        viewModelScope.launch(Dispatchers.IO) {
            repo.checkAndUnlock(snapTotal, snapToday)
            refreshStats()
        }
    }

    fun incrementStore() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.incrementMerit()
            repo.recordMerit(1)
        }
    }

    fun consumeToastEvents(): List<String> {
        val events = _uiState.toastEvents
        if (events.isNotEmpty()) {
            _uiState = _uiState.copy(toastEvents = emptyList())
        }
        return events
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
        val days = repo.getRecentDays(30)
        withContext(Dispatchers.Main) {
            _uiState = _uiState.copy(weekTotal = week, monthTotal = month, recentDays = days)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.release()
    }
}
