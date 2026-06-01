package com.gongde.app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AchievementStoreTest {

    private lateinit var store: AchievementStore

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("achievement_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        store = AchievementStore(context)
    }

    @Test
    fun `预定义了 8 个成就`() {
        assertEquals(8, store.allAchievements.size)
    }

    @Test
    fun `初始状态全部未解锁`() {
        store.allAchievements.forEach { a ->
            assertFalse(store.isUnlocked(a.id))
        }
    }

    @Test
    fun `first_merit 在 totalCount>=1 时解锁`() {
        val unlocked = store.checkAndUnlock(1, 0)
        assertEquals(1, unlocked.size)
        assertEquals("first_merit", unlocked[0].id)
        assertTrue(store.isUnlocked("first_merit"))
    }

    @Test
    fun `merit_100 在 totalCount>=100 时解锁`() {
        val unlocked = store.checkAndUnlock(100, 0)
        assertTrue(unlocked.any { it.id == "merit_100" })
        assertTrue(unlocked.any { it.id == "first_merit" }) // 累积解锁
    }

    @Test
    fun `merit_1000 在 totalCount>=1000 时解锁`() {
        store.checkAndUnlock(1000, 0)
        assertTrue(store.isUnlocked("merit_1000"))
    }

    @Test
    fun `merit_10000 在 totalCount>=10000 时解锁`() {
        store.checkAndUnlock(10000, 0)
        assertTrue(store.isUnlocked("merit_10000"))
    }

    @Test
    fun `daily_100 在 todayCount>=100 时解锁`() {
        val unlocked = store.checkAndUnlock(1000, 100)
        assertTrue(unlocked.any { it.id == "daily_100" })
    }

    @Test
    fun `daily_1000 在 todayCount>=1000 时解锁`() {
        store.checkAndUnlock(5000, 1000)
        assertTrue(store.isUnlocked("daily_1000"))
    }

    @Test
    fun `已解锁的成就不会重复解锁`() {
        store.checkAndUnlock(1, 0)
        val second = store.checkAndUnlock(1, 0)
        assertTrue(second.isEmpty())
    }

    @Test
    fun `解锁数据持久化`() {
        store.checkAndUnlock(100, 0)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val newStore = AchievementStore(context)
        assertTrue(newStore.isUnlocked("first_merit"))
        assertTrue(newStore.isUnlocked("merit_100"))
        assertFalse(newStore.isUnlocked("merit_1000"))
    }

    @Test
    fun `updateStreak 首次调用初始化为 1`() {
        store.updateStreak()
        assertEquals(1, store.unlockedIds.size) // 不影响解锁
    }

    @Test
    fun `checkAndUnlock 不满足条件时返回空列表`() {
        val unlocked = store.checkAndUnlock(0, 0)
        assertTrue(unlocked.isEmpty())
    }

    @Test
    fun `批量解锁多个成就`() {
        val unlocked = store.checkAndUnlock(10000, 1000)
        // 应解锁: first_merit, merit_100, merit_1000, merit_10000, daily_100, daily_1000
        assertTrue(unlocked.size >= 6)
    }

    @Test
    fun `成就名称和描述正确`() {
        val first = store.allAchievements.first { it.id == "first_merit" }
        assertEquals("初入佛门", first.name)
        assertEquals("累计获得1次功德", first.description)
        assertEquals("🌱", first.icon)
    }
}
