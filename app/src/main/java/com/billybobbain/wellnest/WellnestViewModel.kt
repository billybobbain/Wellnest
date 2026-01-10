package com.billybobbain.wellnest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.billybobbain.wellnest.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WellnestViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WellnestRepository

    // Current selected profile
    private val _selectedProfileId = MutableStateFlow<Long?>(null)
    val selectedProfileId: StateFlow<Long?> = _selectedProfileId.asStateFlow()

    // All profiles
    val allProfiles: StateFlow<List<Profile>>

    // Current profile data
    val currentProfile: StateFlow<Profile?>
    val medications: StateFlow<List<Medication>>
    val appointments: StateFlow<List<Appointment>>
    val contacts: StateFlow<List<Contact>>
    val healthProfile: StateFlow<HealthProfile?>
    val insurancePolicies: StateFlow<List<InsurancePolicy>>
    val securityCodes: StateFlow<List<SecurityCode>>
    val supplies: StateFlow<List<Supply>>

    // Insurance providers
    val insuranceProviders: StateFlow<List<InsuranceProvider>>

    // Settings
    val settings: StateFlow<Settings?>

    init {
        val database = WellnestDatabase.getDatabase(application)
        repository = WellnestRepository(database.wellnestDao())

        // Initialize settings
        viewModelScope.launch {
            val currentSettings = repository.settings.first()
            if (currentSettings == null) {
                repository.insertSettings(Settings())
            }
        }

        // Load all profiles
        allProfiles = repository.allProfiles.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Load current profile
        currentProfile = selectedProfileId.flatMapLatest { profileId ->
            if (profileId != null) {
                repository.getProfile(profileId)
            } else {
                flowOf(null)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        // Load data for selected profile
        medications = selectedProfileId.flatMapLatest { profileId ->
            if (profileId != null) {
                repository.getMedicationsForProfile(profileId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        appointments = selectedProfileId.flatMapLatest { profileId ->
            if (profileId != null) {
                repository.getAppointmentsForProfile(profileId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        contacts = selectedProfileId.flatMapLatest { profileId ->
            if (profileId != null) {
                repository.getContactsForProfile(profileId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        healthProfile = selectedProfileId.flatMapLatest { profileId ->
            if (profileId != null) {
                repository.getHealthProfile(profileId)
            } else {
                flowOf(null)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        insurancePolicies = selectedProfileId.flatMapLatest { profileId ->
            if (profileId != null) {
                repository.getInsurancePoliciesForProfile(profileId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        securityCodes = selectedProfileId.flatMapLatest { profileId ->
            if (profileId != null) {
                repository.getSecurityCodesForProfile(profileId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        supplies = selectedProfileId.flatMapLatest { profileId ->
            if (profileId != null) {
                repository.getSuppliesForProfile(profileId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        insuranceProviders = repository.allInsuranceProviders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        settings = repository.settings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    // Profile operations
    fun selectProfile(profileId: Long) {
        _selectedProfileId.value = profileId
    }

    fun addProfile(profile: Profile) {
        viewModelScope.launch {
            val id = repository.insertProfile(profile)
            _selectedProfileId.value = id
        }
    }

    fun updateProfile(profile: Profile) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    fun deleteProfile(profile: Profile) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            if (_selectedProfileId.value == profile.id) {
                _selectedProfileId.value = null
            }
        }
    }

    // Medication operations
    fun addMedication(medication: Medication) {
        viewModelScope.launch {
            repository.insertMedication(medication)
        }
    }

    fun updateMedication(medication: Medication) {
        viewModelScope.launch {
            repository.updateMedication(medication)
        }
    }

    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            repository.deleteMedication(medication)
        }
    }

    // Appointment operations
    fun addAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.insertAppointment(appointment)
            // TODO: Schedule notification if reminderEnabled is true
        }
    }

    fun updateAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.updateAppointment(appointment)
            // TODO: Update notification
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.deleteAppointment(appointment)
            // TODO: Cancel notification
        }
    }

    // Contact operations
    fun addContact(contact: Contact) {
        viewModelScope.launch {
            repository.insertContact(contact)
        }
    }

    fun updateContact(contact: Contact) {
        viewModelScope.launch {
            repository.updateContact(contact)
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }

    // HealthProfile operations
    fun addOrUpdateHealthProfile(healthProfile: HealthProfile) {
        viewModelScope.launch {
            val existing = repository.getHealthProfile(healthProfile.profileId).first()
            if (existing == null) {
                repository.insertHealthProfile(healthProfile)
            } else {
                repository.updateHealthProfile(healthProfile.copy(id = existing.id))
            }
        }
    }

    // InsuranceProvider operations
    fun addInsuranceProvider(provider: InsuranceProvider) {
        viewModelScope.launch {
            repository.insertInsuranceProvider(provider)
        }
    }

    fun updateInsuranceProvider(provider: InsuranceProvider) {
        viewModelScope.launch {
            repository.updateInsuranceProvider(provider)
        }
    }

    fun deleteInsuranceProvider(provider: InsuranceProvider) {
        viewModelScope.launch {
            repository.deleteInsuranceProvider(provider)
        }
    }

    // InsurancePolicy operations
    fun addInsurancePolicy(policy: InsurancePolicy) {
        viewModelScope.launch {
            repository.insertInsurancePolicy(policy)
        }
    }

    fun updateInsurancePolicy(policy: InsurancePolicy) {
        viewModelScope.launch {
            repository.updateInsurancePolicy(policy)
        }
    }

    fun deleteInsurancePolicy(policy: InsurancePolicy) {
        viewModelScope.launch {
            repository.deleteInsurancePolicy(policy)
        }
    }

    // SecurityCode operations
    fun addSecurityCode(securityCode: SecurityCode) {
        viewModelScope.launch {
            repository.insertSecurityCode(securityCode)
        }
    }

    fun updateSecurityCode(securityCode: SecurityCode) {
        viewModelScope.launch {
            repository.updateSecurityCode(securityCode)
        }
    }

    fun deleteSecurityCode(securityCode: SecurityCode) {
        viewModelScope.launch {
            repository.deleteSecurityCode(securityCode)
        }
    }

    // Supply operations
    fun addSupply(supply: Supply) {
        viewModelScope.launch {
            repository.insertSupply(supply)
        }
    }

    fun updateSupply(supply: Supply) {
        viewModelScope.launch {
            repository.updateSupply(supply)
        }
    }

    fun deleteSupply(supply: Supply) {
        viewModelScope.launch {
            repository.deleteSupply(supply)
        }
    }

    // Settings operations
    fun updateTheme(theme: String) {
        viewModelScope.launch {
            val currentSettings = settings.value ?: Settings()
            repository.updateSettings(currentSettings.copy(selectedTheme = theme))
        }
    }
}
