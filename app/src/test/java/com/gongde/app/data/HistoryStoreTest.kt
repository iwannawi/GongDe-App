package com.gongde.app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RunWith(RobolectricTestRunner::class)
class HistoryStoreTest {

    private lateinit var store: HistoryStore
    private val today = LocalDate.now().toString()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("history_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        store = HistoryStore(context)
    }

    @Test
    fun `recordMerit 正确记录当天数据`() {
        store.recordMerit(today, 5)
        val days = store.getRecentDays(1)
        assertEquals(1, days.size)
        assertEquals(5, days[0].second)
    }

    @Test
    fun `recordMerit 累加而非覆盖`() {
        store.recordMerit(today, 3)
        store.recordMerit(today, 7)
        val days = store.getRecentDays(1)
        assertEquals(10, days[0].second)
    }

    @Test
    fun `getRecentDays 返回正确天数`() {
        val days = store.getRecentDays(7)
        assertEquals(7, days.size)
    }

    @Test
    fun `getRecentDays 无数据时返回零`() {
        val days = store.getRecentDays(5)
        assertTrue(days.all { it.second == 0 })
    }

    @Test
    fun `getWeekTotal 汇总正确`() {
        store.recordMerit(today, 20)
        assertEquals(20, store.getWeekTotal())
    }

    @Test
    fun `getMonthTotal 汇总正确`() {
        store.recordMerit(today, 50)
        assertEquals(50, store.getMonthTotal())
    }

    @Test
    fun `多天数据汇总正确`() {
        val yesterday = LocalDate.now().minusDays(1).toString()
        store.recordMerit(today, 10)
        store.recordMerit(yesterday, 20)
        assertEquals(30, store.getWeekTotal())
    }

    @Test
    fun `cleanup 删除旧数据保留新数据`() {
        val oldDate = LocalDate.now().minusDays(100).toString()
        val recentDate = LocalDate.now().minusDays(10).toString()
        store.recordMerit(oldDate, 100)
        store.recordMerit(recentDate, 50)
        store.cleanup(90)
        // 100 天前的数据应被清理，10 天前的应保留
        val map = store.getHistory()
        assertFalse(map.containsKey(oldDate))
        assertEquals(50, map[recentDate])
    }

    @Test
    fun `数据持久化验证`() {
        store.recordMerit(today, 42)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val newStore = HistoryStore(context)
        val days = newStore.getRecentDays(1)
        assertEquals(42, days[0].second)
    }

    @Test
    fun `getHistory 按日期降序排列`() {
        val dates = listOf(
            LocalDate.now().minusDays(2).toString(),
            LocalDate.now().toString(),
            LocalDate.now().minusDays(1).toString()
        )
        dates.forEachIndexed { i, d -> store.recordMerit(d, (i + 1) * 10) }
        val history = store.getHistory()
        val keys = history.keys.toList()
        // 最新的应该在前
        assertTrue(keys[0] >= keys[1])
        assertTrue(keys[1] >= keys[2])
    }
}
