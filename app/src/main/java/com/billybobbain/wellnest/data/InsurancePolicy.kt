package com.billybobbain.wellnest.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "insurance_policies",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = InsuranceProvider::class,
            parentColumns = ["id"],
            childColumns = ["providerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId"), Index("providerId")]
)
data class InsurancePolicy(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val providerId: Long,
    val policyNumber: String,
    val memberPhone: String? = null,
    val providerPhone: String? = null,
    val coverageType: String? = null, // PPO, HMO, etc.
    val insuranceType: String? = null, // Medical, Dental, Vision, Medicare, etc.
    val notes: String? = null,
    val frontCardPhotoUri: String? = null,
    val backCardPhotoUri: String? = null
)
