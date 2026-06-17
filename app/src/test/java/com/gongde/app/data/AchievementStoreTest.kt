package com.gongde.app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
class AchievementStoreTest {

    private lateinit var store: AchievementStore

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("achievement_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        store = AchievementStore(context)
    }

    @Test
    fun `predefines current achievements`() {
        assertEquals(6, store.allAchievements.size)
        assertTrue(store.allAchievements.any { it.id == "first_merit" })
        assertTrue(store.allAchievements.any { it.id == "merit_1000" })
        assertTrue(store.allAchievements.any { it.id == "daily_100" })
        assertTrue(store.allAchievements.any { it.id == "daily_1000" })
        assertTrue(store.allAchievements.any { it.id == "streak_7" })
        assertTrue(store.allAchievements.any { it.id == "streak_30" })
    }

    @Test
    fun `initial state is locked`() {
        store.allAchievements.forEach { achievement ->
            assertFalse(store.isUnlocked(achievement.id))
        }
    }

    @Test
    fun `first merit unlocks when total count reaches one`() {
        val unlocked = store.checkAndUnlock(totalCount = 1, todayCount = 0, streak = 0)

        assertEquals(listOf("first_merit"), unlocked.map { it.id })
        assertTrue(store.isUnlocked("first_merit"))
    }

    @Test
    fun `total and daily achievements unlock at thresholds`() {
        val unlocked = store.checkAndUnlock(totalCount = 1000, todayCount = 1000, streak = 0)
            .map { it.id }

        assertTrue(unlocked.contains("first_merit"))
        assertTrue(unlocked.contains("merit_1000"))
        assertTrue(unlocked.contains("daily_100"))
        assertTrue(unlocked.contains("daily_1000"))
    }

    @Test
    fun `streak achievements unlock at thresholds`() {
        val sevenDay = store.checkAndUnlock(totalCount = 0, todayCount = 0, streak = 7)
            .map { it.id }
        val thirtyDay = store.checkAndUnlock(totalCount = 0, todayCount = 0, streak = 30)
            .map { it.id }

        assertTrue(sevenDay.contains("streak_7"))
        assertTrue(thirtyDay.contains("streak_30"))
    }

    @Test
    fun `unlocked achievements are not emitted again`() {
        store.checkAndUnlock(totalCount = 1, todayCount = 0, streak = 0)

        val second = store.checkAndUnlock(totalCount = 1, todayCount = 0, streak = 0)

        assertTrue(second.isEmpty())
    }

    @Test
    fun `unlocked achievement ids persist`() {
        store.checkAndUnlock(totalCount = 1000, todayCount = 100, streak = 7)
        val context = ApplicationProvider.getApplicationContext<Context>()

        val newStore = AchievementStore(context)

        assertTrue(newStore.isUnlocked("first_merit"))
        assertTrue(newStore.isUnlocked("merit_1000"))
        assertTrue(newStore.isUnlocked("daily_100"))
        assertTrue(newStore.isUnlocked("streak_7"))
        assertFalse(newStore.isUnlocked("daily_1000"))
    }

    @Test
    fun `achievement copy matches product text`() {
        val first = store.allAchievements.first { it.id == "first_merit" }

        assertEquals("新手上路", first.name)
        assertEquals("累计获得1次功德", first.description)
        assertEquals("🌱", first.icon)
    }
}
