package com.vyvyvi.yogabook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_table")
data class Routine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val pose_id: Long,
    val duration: Int
)

data class RoutineWithPose(
    val rid: Long,
    val name: String,
    val hindiName: String,
    val description: String,
    val imageFilename: String,
    var duration: Int,
    val id: Long)