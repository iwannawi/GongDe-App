package com.gongde.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: DailyHistory)

    @Query("SELECT * FROM daily_history ORDER BY date DESC")
    suspend fun getAll(): List<DailyHistory>

    @Query("SELECT * FROM daily_history WHERE date >= :fromDate ORDER BY date DESC")
    suspend fun getFrom(fromDate: String): List<DailyHistory>

    @Query("SELECT COALESCE(SUM(count), 0) FROM daily_history WHERE date >= :fromDate")
    suspend fun getTotalFrom(fromDate: String): Int

    @Query("DELETE FROM daily_history WHERE date < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: String)
}
