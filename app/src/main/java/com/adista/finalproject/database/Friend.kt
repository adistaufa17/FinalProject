package com.adista.finalproject.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String,
    var school: String,
    var bio: String,
    var photo: String
)
