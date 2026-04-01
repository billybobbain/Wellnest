package com.billybobbain.wellnest.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recurring_appointments",
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
data class RecurringAppointment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val title: String,
    val timeOfDay: Long,              // Time in milliseconds since midnight (e.g., 15:00 = 54000000)
    val daysOfWeek: String,           // Comma-separated: "2,4" for Tue/Thu, "6" for Sat (1=Sun, 7=Sat)
    val doctorId: Long? = null,
    val locationId: Long? = null,
    val location: String? = null,     // Legacy text field for backward compatibility
    val notes: String? = null,
    val reminderEnabled: Boolean = false,
    val reminderMinutesBefore: Int = 60,
    val isActive: Boolean = true,     // Soft delete flag
    val icon: String? = null          // Emoji icon for visual identification
)
