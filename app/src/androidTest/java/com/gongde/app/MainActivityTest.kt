package com.gongde.app

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test

class MainActivityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homePrioritizesDailyProgressAndCoreModes() {
        composeRule.onNodeWithText("今日功德").assertIsDisplayed()
        composeRule.onNodeWithText("专注").assertIsDisplayed()
        composeRule.onNodeWithText("ASMR").assertIsDisplayed()
        composeRule.onAllNodesWithText("清零功德计数").assertCountEquals(0)
    }

    @Test
    fun recordNavigationShowsHistoryBeforeAchievements() {
        composeRule.onNodeWithText("记录").performClick()
        composeRule.onNodeWithText("近期记录").assertIsDisplayed()
        composeRule.onNodeWithText("成就进度").assertIsDisplayed()
        composeRule.onNodeWithText("分享今日").performScrollTo().assertIsDisplayed()
    }
}
