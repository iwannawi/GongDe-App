package com.gongde.app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class MeritStoreTest {

    private lateinit var store: MeritStore

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("merit_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        store = MeritStore(context)
    }

    @Test
    fun `initial counts are zero`() {
        assertEquals(0, store.totalCount)
        assertEquals(0, store.todayCount)
    }

    @Test
    fun `increment updates total and today counts`() {
        val (total, today) = store.increment()

        assertEquals(1, total)
        assertEquals(1, today)
        assertEquals(1, store.totalCount)
        assertEquals(1, store.todayCount)
    }

    @Test
    fun `multiple increments accumulate`() {
        repeat(100) { store.increment() }

        assertEquals(100, store.totalCount)
        assertEquals(100, store.todayCount)
    }

    @Test
    fun `reset clears total and today counts`() {
        repeat(10) { store.increment() }

        store.reset()

        assertEquals(0, store.totalCount)
        assertEquals(0, store.todayCount)
    }

    @Test
    fun `counts persist across store recreation`() {
        repeat(3) { store.increment() }
        val context = ApplicationProvider.getApplicationContext<Context>()

        val newStore = MeritStore(context)

        assertEquals(3, newStore.totalCount)
        assertEquals(3, newStore.todayCount)
    }
}
