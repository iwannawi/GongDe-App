package com.gongde.app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class PreferencesStoreTest {

    private lateinit var store: PreferencesStore

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        store = PreferencesStore(context)
        runTest {
            store.setHapticEnabled(true)
            store.setSwitchType("blue")
            store.setThemeId("morning_mist")
            store.setLastActiveDate("")
            store.setStreak(0)
            store.setFirstPressCompleted(false)
            store.setDailyGoalCompletedDate("")
        }
    }

    @Test
    fun `settings can be read and written`() = runTest {
        store.setHapticEnabled(false)
        store.setSwitchType("red")
        store.setThemeId("sky_blue")

        assertFalse(store.getHapticEnabled())
        assertEquals("red", store.getSwitchType())
        assertEquals("sky_blue", store.getThemeId())
    }

    @Test
    fun `streak fields can be read and written`() = runTest {
        store.setLastActiveDate("2026-06-17")
        store.setStreak(7)

        assertEquals("2026-06-17", store.getLastActiveDate())
        assertEquals(7, store.getStreak())
    }

    @Test
    fun `setup restores expected defaults`() = runTest {
        assertTrue(store.getHapticEnabled())
        assertEquals("blue", store.getSwitchType())
        assertEquals("morning_mist", store.getThemeId())
        assertEquals("", store.getLastActiveDate())
        assertEquals(0, store.getStreak())
        assertFalse(store.getFirstPressCompleted())
        assertEquals("", store.getDailyGoalCompletedDate())
    }

    @Test
    fun `onboarding and daily goal state persist`() = runTest {
        store.setFirstPressCompleted(true)
        store.setDailyGoalCompletedDate("2026-06-18")

        assertTrue(store.getFirstPressCompleted())
        assertEquals("2026-06-18", store.getDailyGoalCompletedDate())
    }
}
