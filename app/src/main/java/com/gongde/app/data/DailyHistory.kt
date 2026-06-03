package com.gongde.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_history")
data class DailyHistory(
    @PrimaryKey val date: String,   // ISO_LOCAL_DATE 格式，如 "2026-06-04"
    val count: Int
)
