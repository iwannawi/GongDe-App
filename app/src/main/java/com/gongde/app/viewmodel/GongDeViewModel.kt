package com.gongde.app.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gongde.app.analytics.AnalyticsEvent
import com.gongde.app.analytics.AnalyticsTracker
import com.gongde.app.GongDeApplication
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
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate

sealed class SettingsAction {
    data class SetHaptic(val enabled: Boolean) : SettingsAction()
    data class SetSwitchType(val type: String) : SettingsAction()
    data class SetTheme(val themeId: String) : SettingsAction()
}

data class MeritUiState(
    val todayGoal: Int = 100,
    val totalCount: Int = 0,
    val todayCount: Int = 0,
    val triggerCount: Int = 0,
    val hapticEnabled: Boolean = true,
    val switchType: SwitchType = SwitchType.BLUE,
    val themeId: String = "morning_mist",
    val weekTotal: Int = 0,
    val monthTotal: Int = 0,
    val recentDays: List<Pair<String, Int>> = emptyList(),
    val completedAchievementIds: Set<String> = emptySet(),
    val streak: Int = 0,
    val showFirstPressHint: Boolean = true,
    val dailyGoalCompleted: Boolean = false,
    val toastEvents: List<String> = emptyList()
)

class GongDeViewModel(
    private val repo: GongDeRepository,
    val achievementStore: AchievementStore,
    private val analytics: AnalyticsTracker
) : ViewModel() {

    class Factory(private val app: GongDeApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val meritStore = MeritStore(app)
            val achievementStore = AchievementStore(app)
            val prefsStore = PreferencesStore(app)
            val historyDao = AppDatabase.getInstance(app).historyDao()
            val repo = GongDeRepository(meritStore, historyDao, prefsStore, achievementStore)
            return GongDeViewModel(repo, achievementStore, app.analyticsTracker) as T
        }
    }

    val soundEngine = SoundEngine()
    private val achievementMutex = Mutex()
    private val meritMutex = Mutex()
    private var sessionPressCount = 0

    private var _uiState by mutableStateOf(MeritUiState())
    val uiState: MeritUiState get() = _uiState

    init {
        analytics.track(AnalyticsEvent.APP_OPEN)
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateStreak()
            val haptic = repo.getHapticEnabled()
            val switch = repo.getSwitchType()
            val theme = repo.getThemeId()
            val switchType = try { SwitchType.valueOf(switch.uppercase()) } catch (_: Exception) { SwitchType.BLUE }
            val week = repo.getWeekTotal()
            val month = repo.getMonthTotal()
            val days = repo.getRecentDays(30)
            val total = repo.totalCount
            val today = repo.todayCount
            repo.checkAndUnlock(total, today)
            val completedAchievementIds = repo.getCompletedAchievementIds(total, today)
            val streak = repo.getStreak()
            val showFirstPressHint = !repo.getFirstPressCompleted()
            val todayDate = LocalDate.now().toString()
            var dailyGoalCompleted = repo.getDailyGoalCompletedDate() == todayDate
            if (today >= 100 && !dailyGoalCompleted) {
                repo.setDailyGoalCompletedDate(todayDate)
                dailyGoalCompleted = true
            }

            withContext(Dispatchers.Main) {
                _uiState = _uiState.copy(
                    totalCount = total,
                    todayCount = today,
                    hapticEnabled = haptic,
                    switchType = switchType,
                    themeId = theme,
                    weekTotal = week,
                    monthTotal = month,
                    recentDays = days,
                    completedAchievementIds = completedAchievementIds,
                    streak = streak,
                    showFirstPressHint = showFirstPressHint,
                    dailyGoalCompleted = dailyGoalCompleted
                )
            }
        }

        soundEngine.warmUp()
        viewModelScope.launch(Dispatchers.IO) { repo.cleanupOldHistory() }
    }

    fun incrementMerit() {
        sessionPressCount++
        if (_uiState.showFirstPressHint) {
            _uiState = _uiState.copy(showFirstPressHint = false)
            analytics.track(AnalyticsEvent.FIRST_VALID_PRESS)
            viewModelScope.launch(Dispatchers.IO) { repo.setFirstPressCompleted(true) }
        }
        if (sessionPressCount == 10) analytics.track(AnalyticsEvent.PRESS_10_REACHED)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (newTotal, newToday) = meritMutex.withLock {
                    repo.incrementMerit().also { repo.recordMerit(1) }
                }
                withContext(Dispatchers.Main) {
                    _uiState = _uiState.copy(
                        totalCount = newTotal,
                        todayCount = newToday,
                        triggerCount = _uiState.triggerCount + 1
                    )
                }
                repo.updateStreak()
                val todayDate = LocalDate.now().toString()
                val reachedDailyGoal = newToday >= _uiState.todayGoal &&
                    repo.getDailyGoalCompletedDate() != todayDate
                if (reachedDailyGoal) {
                    repo.setDailyGoalCompletedDate(todayDate)
                    analytics.track(AnalyticsEvent.DAILY_GOAL_COMPLETED)
                }
                achievementMutex.lock()
                try {
                    val unlocked = repo.checkAndUnlock(newTotal, newToday)
                    val completedIds = repo.getCompletedAchievementIds(newTotal, newToday)
                    withContext(Dispatchers.Main) {
                        val messages = buildList {
                            if (reachedDailyGoal) add("今日 100 次目标完成")
                            addAll(unlocked.filterNot { reachedDailyGoal && it.id == "daily_100" }
                                .map { a -> "成就解锁：${a.name}" })
                        }
                        _uiState = _uiState.copy(
                            completedAchievementIds = completedIds,
                            streak = repo.getStreak(),
                            dailyGoalCompleted = _uiState.dailyGoalCompleted || reachedDailyGoal,
                            toastEvents = _uiState.toastEvents + messages
                        )
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
        _uiState = _uiState.copy(
            totalCount = 0,
            todayCount = 0,
            completedAchievementIds = _uiState.completedAchievementIds - AchievementStore.DAILY_ACHIEVEMENT_IDS,
            dailyGoalCompleted = false
        )
        viewModelScope.launch(Dispatchers.IO) { refreshStats() }
    }

    fun syncFromStore() {
        viewModelScope.launch(Dispatchers.IO) {
            val (snapTotal, snapToday) = meritMutex.withLock { repo.totalCount to repo.todayCount }
            withContext(Dispatchers.Main) {
                _uiState = _uiState.copy(totalCount = snapTotal, todayCount = snapToday)
            }
            repo.updateStreak()
            val todayDate = LocalDate.now().toString()
            val reachedDailyGoal = snapToday >= _uiState.todayGoal &&
                repo.getDailyGoalCompletedDate() != todayDate
            if (reachedDailyGoal) {
                repo.setDailyGoalCompletedDate(todayDate)
                analytics.track(AnalyticsEvent.DAILY_GOAL_COMPLETED)
            }
            achievementMutex.lock()
            try {
                val unlocked = repo.checkAndUnlock(snapTotal, snapToday)
                val completedIds = repo.getCompletedAchievementIds(snapTotal, snapToday)
                withContext(Dispatchers.Main) {
                    val messages = buildList {
                        if (reachedDailyGoal) add("今日 100 次目标完成")
                        addAll(unlocked.filterNot { reachedDailyGoal && it.id == "daily_100" }
                            .map { a -> "成就解锁：${a.name}" })
                    }
                    _uiState = _uiState.copy(
                        completedAchievementIds = completedIds,
                        streak = repo.getStreak(),
                        dailyGoalCompleted = _uiState.dailyGoalCompleted || reachedDailyGoal,
                        toastEvents = _uiState.toastEvents + messages
                    )
                }
            } finally {
                achievementMutex.unlock()
            }
            refreshStats()
        }
    }

    fun incrementStore() {
        viewModelScope.launch(Dispatchers.IO) {
            meritMutex.withLock {
                repo.incrementMerit()
                repo.recordMerit(1)
            }
        }
    }

    fun trackModeOpen(mode: String) {
        analytics.track(AnalyticsEvent.MODE_OPEN, mapOf("mode" to mode))
    }

    fun trackShareStarted() = analytics.track(AnalyticsEvent.SHARE_STARTED)

    fun trackShareCompleted() = analytics.track(AnalyticsEvent.SHARE_COMPLETED)

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
