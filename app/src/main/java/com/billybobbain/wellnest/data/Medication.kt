package com.billybobbain.wellnest.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medications",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId")]
)
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val drugName: String,
    val dosage: String? = null,
    val frequency: String? = null,
    val prescribingDoctor: String? = null,
    val pharmacy: String? = null,
    val startDate: Long? = null,
    val refillDate: Long? = null,
    val notes: String? = null
)
