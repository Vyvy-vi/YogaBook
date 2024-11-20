package com.vyvyvi.yogabook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = false)
    val username: String,
    val email: String,
    val password: String,
    val avatar: ByteArray,
    val trackLocation: Boolean = false
)


