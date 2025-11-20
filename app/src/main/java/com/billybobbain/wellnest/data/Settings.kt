package com.billybobbain.wellnest.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey
    val id: Int = 1, // Only one settings row
    val selectedTheme: String = "Teal"
)
