package com.gongde.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class HistoryStoreTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var dao: HistoryDao
    private lateinit var repo: GongDeRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("merit_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("achievement_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.historyDao()
        repo = GongDeRepository(
            MeritStore(context),
            dao,
            PreferencesStore(context),
            AchievementStore(context)
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `recordMerit records today's count`() = runTest {
        repo.recordMerit(5)

        val days = repo.getRecentDays(1)

        assertEquals(1, days.size)
        assertEquals(5, days[0].second)
    }

    @Test
    fun `recordMerit accumulates instead of replacing`() = runTest {
        repo.recordMerit(3)
        repo.recordMerit(7)

        assertEquals(10, repo.getRecentDays(1)[0].second)
    }

    @Test
    fun `recordMerit handles concurrent increments`() = runTest {
        val jobs = List(100) {
            async { repo.recordMerit(1) }
        }

        jobs.awaitAll()

        assertEquals(100, repo.getRecentDays(1)[0].second)
    }

    @Test
    fun `getRecentDays returns requested day count with zero fill`() = runTest {
        val days = repo.getRecentDays(5)

        assertEquals(5, days.size)
        assertTrue(days.all { it.second == 0 })
    }

    @Test
    fun `week and month totals include recent records`() = runTest {
        val today = LocalDate.now().toString()
        val yesterday = LocalDate.now().minusDays(1).toString()
        dao.upsert(DailyHistory(today, 10))
        dao.upsert(DailyHistory(yesterday, 20))

        assertEquals(30, repo.getWeekTotal())
        assertEquals(30, repo.getMonthTotal())
    }

    @Test
    fun `cleanup removes records older than retention window`() = runTest {
        val oldDate = LocalDate.now().minusDays(100).toString()
        val recentDate = LocalDate.now().minusDays(10).toString()
        dao.upsert(DailyHistory(oldDate, 100))
        dao.upsert(DailyHistory(recentDate, 50))

        repo.cleanupOldHistory(90)

        val records = dao.getAll().associateBy { it.date }
        assertFalse(records.containsKey(oldDate))
        assertEquals(50, records[recentDate]?.count)
    }

    @Test
    fun `dao returns history in descending date order`() = runTest {
        val dates = listOf(
            LocalDate.now().minusDays(2).toString(),
            LocalDate.now().toString(),
            LocalDate.now().minusDays(1).toString()
        )
        dates.forEachIndexed { index, date ->
            dao.upsert(DailyHistory(date, (index + 1) * 10))
        }

        val keys = dao.getAll().map { it.date }

        assertTrue(keys[0] >= keys[1])
        assertTrue(keys[1] >= keys[2])
    }
}
