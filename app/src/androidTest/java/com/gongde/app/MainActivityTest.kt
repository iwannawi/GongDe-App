package com.gongde.app

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
    fun homePrioritizesDailyReliefRound() {
        composeRule.onNodeWithText("今日解压任务").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("机械键帽，点击上表面释放压力").assertIsDisplayed()
        composeRule.onNodeWithText("抽一张情绪签").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("开始一轮").performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("专注").assertCountEquals(0)
        composeRule.onAllNodesWithText("ASMR").assertCountEquals(0)
        composeRule.onAllNodesWithText("清零功德计数").assertCountEquals(0)
    }

    @Test
    fun collectionNavigationShowsCollectionsAndAchievements() {
        composeRule.onNodeWithText("图鉴").performClick()
        composeRule.onNodeWithText("情绪签图鉴").assertIsDisplayed()
        composeRule.onNodeWithText("键帽收藏").assertIsDisplayed()
        composeRule.onNodeWithText("成就进度").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun recordNavigationShowsHistoryAndShare() {
        composeRule.onNodeWithText("记录").performClick()
        composeRule.onNodeWithText("近期记录").assertIsDisplayed()
        composeRule.onNodeWithText("分享今日").performScrollTo().assertIsDisplayed()
    }
}
