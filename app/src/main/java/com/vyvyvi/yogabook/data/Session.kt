package com.vyvyvi.yogabook.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.vyvyvi.yogabook.utils.Converters
import java.time.LocalDate

@Entity(tableName = "session_table")
@TypeConverters(Converters::class)
data class Session(
    @PrimaryKey(autoGenerate = true)
    var session_id: Long = 0,
    val username: String,
    val completed: Boolean = false,
    val date: LocalDate? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val location: String? = null
)