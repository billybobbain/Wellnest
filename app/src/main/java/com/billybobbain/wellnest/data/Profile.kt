package com.billybobbain.wellnest.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val photoUri: String? = null,
    val notes: String? = null,
    val createdDate: Long = System.currentTimeMillis(),

    // Room dimensions
    val roomLength: String? = null,
    val roomWidth: String? = null,
    val roomHeight: String? = null,
    val windowWidth: String? = null,
    val windowHeight: String? = null,
    val roomNotes: String? = null
)
