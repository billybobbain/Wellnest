package com.billybobbain.wellnest.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "health_profiles",
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
data class HealthProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val height: String? = null,
    val weight: String? = null,
    val bloodType: String? = null,
    val allergies: String? = null,
    val medicalConditions: String? = null,
    val emergencyContact: String? = null,
    val emergencyPhone: String? = null,
    val notes: String? = null
)
