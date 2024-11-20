package com.vyvyvi.yogabook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_item_table")
data class SessionItem(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val sessionId: Long = 0,
    var duration: Int,
    val name: String,
    val hindiName: String,
    val imgFilename: String,
    val description: String
)

