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
        )
    ],
    indices = [Index("profileId")]
)
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val title: String,
    val dateTime: Long,
    val location: String? = null,
    val notes: String? = null,
    val reminderEnabled: Boolean = false,
    val reminderMinutesBefore: Int = 60
)
