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
        ),
        ForeignKey(
            entity = Doctor::class,
            parentColumns = ["id"],
            childColumns = ["doctorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("profileId"), Index("doctorId")]
)
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val drugName: String,
    val dosage: String? = null,
    val frequency: String? = null,
    val doctorId: Long? = null,
    val pharmacy: String? = null,
    val startDate: Long? = null,
    val refillDate: Long? = null,
    val notes: String? = null,
    val classification: String? = null,
    val diagnosis: String? = null
)
