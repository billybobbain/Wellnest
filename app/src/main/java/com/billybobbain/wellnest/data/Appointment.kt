package com.billybobbain.wellnest.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "appointments",
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
        ),
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("profileId"), Index("doctorId"), Index("locationId")]
)
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val title: String,
    val dateTime: Long,
    val location: String? = null,  // Keep for backward compatibility, will deprecate later
    val doctorId: Long? = null,
    val locationId: Long? = null,  // New: link to Location entity
    val notes: String? = null,
    val reminderEnabled: Boolean = false,
    val reminderMinutesBefore: Int = 60,
    val isArchived: Boolean = false,  // New: soft delete
    val milesDriven: Double? = null,  // New: actual miles driven (for mileage tracking)
    val wasAttended: Boolean = true   // New: did you actually attend? (defaults to true)
)
