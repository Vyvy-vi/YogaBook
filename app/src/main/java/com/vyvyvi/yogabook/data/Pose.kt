package com.vyvyvi.yogabook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pose_table")
data class Pose(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val hindiName: String,
    val description: String,
    val imageFilename: String,
//    val videoUrl: String
)