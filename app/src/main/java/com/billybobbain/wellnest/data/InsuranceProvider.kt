package com.billybobbain.wellnest.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "insurance_providers")
data class InsuranceProvider(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val address: String? = null,
    val notes: String? = null
)
