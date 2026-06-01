package com.gongde.app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MeritStoreTest {

    private lateinit var store: MeritStore

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // 清除旧数据
        context.getSharedPreferences("merit_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        store = MeritStore(context)
    }

    @Test
    fun `初始值为零`() {
        assertEquals(0, store.totalCount)
        assertEquals(0, store.todayCount)
    }

    @Test
    fun `increment 正确递增`() {
        val (total, today) = store.increment()
        assertEquals(1, total)
        assertEquals(1, today)
    }

    @Test
    fun `连续 increment 累计正确`() {
        repeat(5) { store.increment() }
        val (total, today) = store.increment()
        assertEquals(6, total)
        assertEquals(6, today)
    }

    @Test
    fun `reset 将所有计数清零`() {
        repeat(10) { store.increment() }
        store.reset()
        assertEquals(0, store.totalCount)
        assertEquals(0, store.todayCount)
    }

    @Test
    fun `设置项默认值正确`() {
        assertTrue(store.hapticEnabled)
        assertEquals("blue", store.switchType)
        assertEquals("deep_purple", store.themeId)
        assertFalse(store.asmrEnabled)
    }

    @Test
    fun `设置项可正确读写`() {
        store.hapticEnabled = false
        assertFalse(store.hapticEnabled)

        store.switchType = "red"
        assertEquals("red", store.switchType)

        store.themeId = "cyber_blue"
        assertEquals("cyber_blue", store.themeId)

        store.asmrEnabled = true
        assertTrue(store.asmrEnabled)
    }

    @Test
    fun `数据持久化验证`() {
        store.increment()
        store.hapticEnabled = false
        store.switchType = "brown"

        // 重新创建 store 模拟进程重启
        val context = ApplicationProvider.getApplicationContext<Context>()
        val newStore = MeritStore(context)
        assertEquals(1, newStore.totalCount)
        assertFalse(newStore.hapticEnabled)
        assertEquals("brown", newStore.switchType)
    }

    @Test
    fun `increment 返回值与内部状态一致`() {
        val (total, today) = store.increment()
        assertEquals(store.totalCount, total)
        assertEquals(store.todayCount, today)
    }

    @Test
    fun `连续快速 increment 不丢数据`() {
        val results = mutableListOf<Pair<Int, Int>>()
        repeat(100) { results.add(store.increment()) }
        assertEquals(100, store.totalCount)
        assertEquals(100, store.todayCount)
        // 验证每一步都递增
        results.forEachIndexed { i, (total, today) ->
            assertEquals(i + 1, total)
            assertEquals(i + 1, today)
        }
    }
}
