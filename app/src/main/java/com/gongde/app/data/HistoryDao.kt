package com.gongde.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: DailyHistory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(entry: DailyHistory): Long

    @Query("UPDATE daily_history SET count = count + :count WHERE date = :date")
    suspend fun incrementCount(date: String, count: Int): Int

    @Query("SELECT * FROM daily_history WHERE date = :date LIMIT 1")
    suspend fun getForDate(date: String): DailyHistory?

    @Query("SELECT * FROM daily_history ORDER BY date DESC")
    suspend fun getAll(): List<DailyHistory>

    @Query("SELECT * FROM daily_history WHERE date >= :fromDate ORDER BY date DESC")
    suspend fun getFrom(fromDate: String): List<DailyHistory>

    @Query("SELECT COALESCE(SUM(count), 0) FROM daily_history WHERE date >= :fromDate")
    suspend fun getTotalFrom(fromDate: String): Int

    @Query("DELETE FROM daily_history WHERE date < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: String)
}
