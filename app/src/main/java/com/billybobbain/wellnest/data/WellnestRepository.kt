package com.billybobbain.wellnest.data

import kotlinx.coroutines.flow.Flow

class WellnestRepository(private val dao: WellnestDao) {
    // Profile operations
    val allProfiles: Flow<List<Profile>> = dao.getAllProfiles()

    fun getProfile(id: Long): Flow<Profile?> = dao.getProfile(id)

    suspend fun insertProfile(profile: Profile): Long = dao.insertProfile(profile)

    suspend fun updateProfile(profile: Profile) = dao.updateProfile(profile)

    suspend fun deleteProfile(profile: Profile) = dao.deleteProfile(profile)

    // Medication operations
    fun getMedicationsForProfile(profileId: Long): Flow<List<Medication>> =
        dao.getMedicationsForProfile(profileId)

    suspend fun insertMedication(medication: Medication) = dao.insertMedication(medication)

    suspend fun updateMedication(medication: Medication) = dao.updateMedication(medication)

    suspend fun deleteMedication(medication: Medication) = dao.deleteMedication(medication)

    // Appointment operations
    fun getAppointmentsForProfile(profileId: Long): Flow<List<Appointment>> =
        dao.getAppointmentsForProfile(profileId)

    fun getUpcomingAppointments(currentTime: Long): Flow<List<Appointment>> =
        dao.getUpcomingAppointments(currentTime)

    suspend fun insertAppointment(appointment: Appointment) = dao.insertAppointment(appointment)

    suspend fun updateAppointment(appointment: Appointment) = dao.updateAppointment(appointment)

    suspend fun deleteAppointment(appointment: Appointment) = dao.deleteAppointment(appointment)

    // Contact operations
    fun getContactsForProfile(profileId: Long): Flow<List<Contact>> =
        dao.getContactsForProfile(profileId)

    suspend fun insertContact(contact: Contact) = dao.insertContact(contact)

    suspend fun updateContact(contact: Contact) = dao.updateContact(contact)

    suspend fun deleteContact(contact: Contact) = dao.deleteContact(contact)

    // HealthProfile operations
    fun getHealthProfile(profileId: Long): Flow<HealthProfile?> =
        dao.getHealthProfile(profileId)

    suspend fun insertHealthProfile(healthProfile: HealthProfile) =
        dao.insertHealthProfile(healthProfile)

    suspend fun updateHealthProfile(healthProfile: HealthProfile) =
        dao.updateHealthProfile(healthProfile)

    // InsuranceProvider operations
    val allInsuranceProviders: Flow<List<InsuranceProvider>> = dao.getAllInsuranceProviders()

    fun getInsuranceProvider(id: Long): Flow<InsuranceProvider?> = dao.getInsuranceProvider(id)

    suspend fun insertInsuranceProvider(provider: InsuranceProvider): Long =
        dao.insertInsuranceProvider(provider)

    suspend fun updateInsuranceProvider(provider: InsuranceProvider) =
        dao.updateInsuranceProvider(provider)

    suspend fun deleteInsuranceProvider(provider: InsuranceProvider) =
        dao.deleteInsuranceProvider(provider)

    // InsurancePolicy operations
    fun getInsurancePoliciesForProfile(profileId: Long): Flow<List<InsurancePolicy>> =
        dao.getInsurancePoliciesForProfile(profileId)

    suspend fun insertInsurancePolicy(policy: InsurancePolicy) = dao.insertInsurancePolicy(policy)

    suspend fun updateInsurancePolicy(policy: InsurancePolicy) = dao.updateInsurancePolicy(policy)

    suspend fun deleteInsurancePolicy(policy: InsurancePolicy) = dao.deleteInsurancePolicy(policy)

    // SecurityCode operations
    fun getSecurityCodesForProfile(profileId: Long): Flow<List<SecurityCode>> =
        dao.getSecurityCodesForProfile(profileId)

    suspend fun insertSecurityCode(securityCode: SecurityCode) =
        dao.insertSecurityCode(securityCode)

    suspend fun updateSecurityCode(securityCode: SecurityCode) =
        dao.updateSecurityCode(securityCode)

    suspend fun deleteSecurityCode(securityCode: SecurityCode) =
        dao.deleteSecurityCode(securityCode)

    // Supply operations
    fun getSuppliesForProfile(profileId: Long): Flow<List<Supply>> =
        dao.getSuppliesForProfile(profileId)

    suspend fun insertSupply(supply: Supply) = dao.insertSupply(supply)

    suspend fun updateSupply(supply: Supply) = dao.updateSupply(supply)

    suspend fun deleteSupply(supply: Supply) = dao.deleteSupply(supply)

    // Settings operations
    val settings: Flow<Settings?> = dao.getSettings()

    suspend fun insertSettings(settings: Settings) = dao.insertSettings(settings)

    suspend fun updateSettings(settings: Settings) = dao.updateSettings(settings)
}
