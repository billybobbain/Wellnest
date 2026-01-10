package com.billybobbain.wellnest.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WellnestDao {
    // Profile operations
    @Query("SELECT * FROM profiles ORDER BY name ASC")
    fun getAllProfiles(): Flow<List<Profile>>

    @Query("SELECT * FROM profiles WHERE id = :id")
    fun getProfile(id: Long): Flow<Profile?>

    @Insert
    suspend fun insertProfile(profile: Profile): Long

    @Update
    suspend fun updateProfile(profile: Profile)

    @Delete
    suspend fun deleteProfile(profile: Profile)

    // Medication operations
    @Query("SELECT * FROM medications WHERE profileId = :profileId ORDER BY drugName ASC")
    fun getMedicationsForProfile(profileId: Long): Flow<List<Medication>>

    @Insert
    suspend fun insertMedication(medication: Medication)

    @Update
    suspend fun updateMedication(medication: Medication)

    @Delete
    suspend fun deleteMedication(medication: Medication)

    // Appointment operations
    @Query("SELECT * FROM appointments WHERE profileId = :profileId ORDER BY dateTime ASC")
    fun getAppointmentsForProfile(profileId: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE dateTime >= :currentTime ORDER BY dateTime ASC")
    fun getUpcomingAppointments(currentTime: Long): Flow<List<Appointment>>

    @Insert
    suspend fun insertAppointment(appointment: Appointment)

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)

    // Contact operations
    @Query("SELECT * FROM contacts WHERE profileId = :profileId ORDER BY name ASC")
    fun getContactsForProfile(profileId: Long): Flow<List<Contact>>

    @Insert
    suspend fun insertContact(contact: Contact)

    @Update
    suspend fun updateContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    // HealthProfile operations
    @Query("SELECT * FROM health_profiles WHERE profileId = :profileId LIMIT 1")
    fun getHealthProfile(profileId: Long): Flow<HealthProfile?>

    @Insert
    suspend fun insertHealthProfile(healthProfile: HealthProfile)

    @Update
    suspend fun updateHealthProfile(healthProfile: HealthProfile)

    // InsuranceProvider operations
    @Query("SELECT * FROM insurance_providers ORDER BY name ASC")
    fun getAllInsuranceProviders(): Flow<List<InsuranceProvider>>

    @Query("SELECT * FROM insurance_providers WHERE id = :id")
    fun getInsuranceProvider(id: Long): Flow<InsuranceProvider?>

    @Insert
    suspend fun insertInsuranceProvider(provider: InsuranceProvider): Long

    @Update
    suspend fun updateInsuranceProvider(provider: InsuranceProvider)

    @Delete
    suspend fun deleteInsuranceProvider(provider: InsuranceProvider)

    // InsurancePolicy operations
    @Query("SELECT * FROM insurance_policies WHERE profileId = :profileId")
    fun getInsurancePoliciesForProfile(profileId: Long): Flow<List<InsurancePolicy>>

    @Insert
    suspend fun insertInsurancePolicy(policy: InsurancePolicy)

    @Update
    suspend fun updateInsurancePolicy(policy: InsurancePolicy)

    @Delete
    suspend fun deleteInsurancePolicy(policy: InsurancePolicy)

    // SecurityCode operations
    @Query("SELECT * FROM security_codes WHERE profileId = :profileId ORDER BY label ASC")
    fun getSecurityCodesForProfile(profileId: Long): Flow<List<SecurityCode>>

    @Insert
    suspend fun insertSecurityCode(securityCode: SecurityCode)

    @Update
    suspend fun updateSecurityCode(securityCode: SecurityCode)

    @Delete
    suspend fun deleteSecurityCode(securityCode: SecurityCode)

    // Supply operations
    @Query("SELECT * FROM supplies WHERE profileId = :profileId ORDER BY itemName ASC")
    fun getSuppliesForProfile(profileId: Long): Flow<List<Supply>>

    @Insert
    suspend fun insertSupply(supply: Supply)

    @Update
    suspend fun updateSupply(supply: Supply)

    @Delete
    suspend fun deleteSupply(supply: Supply)

    // Settings operations
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<Settings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: Settings)

    @Update
    suspend fun updateSettings(settings: Settings)
}
