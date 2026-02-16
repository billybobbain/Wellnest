package com.billybobbain.wellnest.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctors")
data class Doctor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val specialty: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val notes: String? = null
)
