package com.gongde.app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.time.LocalDate
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
    private lateinit var context: Context
    private var today = LocalDate.of(2026, 6, 18)

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("achievement_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        store = AchievementStore(context) { today }
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

        val newStore = AchievementStore(context) { today }

        assertTrue(newStore.isUnlocked("first_merit"))
        assertTrue(newStore.isUnlocked("merit_1000"))
        assertFalse(newStore.isUnlocked("daily_100"))
        assertTrue(newStore.isUnlocked("streak_7"))
        assertFalse(newStore.isUnlocked("daily_1000"))
    }

    @Test
    fun `daily achievement completion follows current day count`() {
        assertTrue(store.getCompletedIds(totalCount = 100, todayCount = 100, streak = 0).contains("daily_100"))

        today = today.plusDays(1)

        assertFalse(store.getCompletedIds(totalCount = 100, todayCount = 0, streak = 0).contains("daily_100"))
    }

    @Test
    fun `daily achievement is emitted only once per day`() {
        val first = store.checkAndUnlock(totalCount = 100, todayCount = 100, streak = 0)
        val second = store.checkAndUnlock(totalCount = 101, todayCount = 101, streak = 0)

        assertTrue(first.any { it.id == "daily_100" })
        assertFalse(second.any { it.id == "daily_100" })

        today = today.plusDays(1)
        val nextDay = store.checkAndUnlock(totalCount = 200, todayCount = 100, streak = 0)
        assertTrue(nextDay.any { it.id == "daily_100" })
    }

    @Test
    fun `legacy persisted daily achievements are removed`() {
        context.getSharedPreferences("achievement_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("unlocked_json", "[\"first_merit\",\"daily_100\"]")
            .commit()

        val migratedStore = AchievementStore(context) { today }

        assertTrue(migratedStore.isUnlocked("first_merit"))
        assertFalse(migratedStore.isUnlocked("daily_100"))
        assertFalse(migratedStore.getCompletedIds(1, 0, 0).contains("daily_100"))
    }

    @Test
    fun `achievement copy matches product text`() {
        val first = store.allAchievements.first { it.id == "first_merit" }

        assertEquals("新手上路", first.name)
        assertEquals("累计获得 1 次功德", first.description)
        assertEquals("🌱", first.icon)
        assertEquals(AchievementMetric.TOTAL, first.metric)
        assertEquals(1, first.target)
    }

    @Test
    fun `achievement progress uses the configured metric`() {
        val total = store.allAchievements.first { it.id == "merit_1000" }
        val daily = store.allAchievements.first { it.id == "daily_100" }
        val streak = store.allAchievements.first { it.id == "streak_7" }

        assertEquals(640, store.currentValue(total, totalCount = 640, todayCount = 20, streak = 3))
        assertEquals(20, store.currentValue(daily, totalCount = 640, todayCount = 20, streak = 3))
        assertEquals(3, store.currentValue(streak, totalCount = 640, todayCount = 20, streak = 3))
    }
}
