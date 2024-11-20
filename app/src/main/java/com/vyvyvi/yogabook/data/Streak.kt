package com.vyvyvi.yogabook.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.vyvyvi.yogabook.utils.Converters
import java.time.LocalDate


@Entity(tableName = "streak_table")
@TypeConverters(Converters::class)
data class Streak(
    @PrimaryKey(autoGenerate = false)
    val username: String,
    var current_streak: Int = 0,
    var max_streak: Int = 0,
    var last_streak_date: LocalDate? = null
)