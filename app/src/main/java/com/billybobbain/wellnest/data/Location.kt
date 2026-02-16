package com.billybobbain.wellnest.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class Location(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val distanceMiles: Double? = null,  // Calculated from home address
    val phone: String? = null,
    val notes: String? = null
)
