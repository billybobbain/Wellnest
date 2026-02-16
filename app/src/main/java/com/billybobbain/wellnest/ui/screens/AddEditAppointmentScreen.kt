package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.Appointment
import com.billybobbain.wellnest.data.Doctor
import com.billybobbain.wellnest.data.Location
import com.billybobbain.wellnest.utils.LocationUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAppointmentScreen(
    viewModel: WellnestViewModel,
    appointmentId: Long?,
    onNavigateBack: () -> Unit
) {
    val appointments by viewModel.appointments.collectAsState()
    val doctors by viewModel.doctors.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    val selectedProfileId by viewModel.selectedProfileId.collectAsState()
    val existingAppointment = appointments.find { it.id == appointmentId }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf(existingAppointment?.title ?: "") }
    var location by remember { mutableStateOf(existingAppointment?.location ?: "") }
    var selectedDoctorId by remember { mutableStateOf(existingAppointment?.doctorId) }
    var selectedLocationId by remember { mutableStateOf(existingAppointment?.locationId) }
    var notes by remember { mutableStateOf(existingAppointment?.notes ?: "") }
    var dateTime by remember { mutableStateOf(existingAppointment?.dateTime ?: System.currentTimeMillis()) }
    var reminderEnabled by remember { mutableStateOf(existingAppointment?.reminderEnabled ?: false) }
    var reminderMinutes by remember { mutableStateOf(existingAppointment?.reminderMinutesBefore?.toString() ?: "60") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDoctorDialog by remember { mutableStateOf(false) }
    var newDoctorName by remember { mutableStateOf("") }
    var showLocationDialog by remember { mutableStateOf(false) }
    var newLocationName by remember { mutableStateOf("") }
    var newLocationAddress by remember { mutableStateOf("") }
    var newLocationPhone by remember { mutableStateOf("") }
    var geocodingNewLocation by remember { mutableStateOf(false) }

    // Track locations for selected doctor
    val doctorLocations by produceState<List<Location>>(
        initialValue = emptyList(),
        key1 = selectedDoctorId
    ) {
        value = if (selectedDoctorId != null) {
            viewModel.repository.getLocationsForDoctor(selectedDoctorId!!).first()
        } else {
            emptyList()
        }
    }

    // Clear location selection if it's not valid for the newly selected doctor
    LaunchedEffect(selectedDoctorId, doctorLocations) {
        if (selectedDoctorId != null && selectedLocationId != null) {
            if (!doctorLocations.any { it.id == selectedLocationId }) {
                selectedLocationId = null
            }
        }
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val calendar = Calendar.getInstance().apply { timeInMillis = dateTime }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (appointmentId == null) "Add Appointment" else "Edit Appointment") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            Text("Date & Time", style = MaterialTheme.typography.titleSmall)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text("Date", style = MaterialTheme.typography.labelSmall)
                        Text(dateFormat.format(Date(dateTime)))
                    }
                }

                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text("Time", style = MaterialTheme.typography.labelSmall)
                        Text(timeFormat.format(Date(dateTime)))
                    }
                }
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            // Doctor selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Doctor (Optional)", style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = { showDoctorDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Doctor")
                }
            }

            if (doctors.isEmpty()) {
                Text("No doctors yet. Add one using the + button above.", style = MaterialTheme.typography.bodySmall)
            } else {
                doctors.forEach { doctor ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDoctorId == doctor.id,
                            onClick = { selectedDoctorId = doctor.id }
                        )
                        Text(
                            text = doctor.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            // Option to deselect doctor
            if (selectedDoctorId != null) {
                TextButton(onClick = { selectedDoctorId = null }) {
                    Text("Clear Doctor")
                }
            }

            // Location selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Location (Optional)", style = MaterialTheme.typography.titleSmall)
                    if (selectedDoctorId != null) {
                        val selectedDoctor = doctors.find { it.id == selectedDoctorId }
                        Text(
                            "Showing locations for ${selectedDoctor?.name ?: "selected doctor"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(onClick = { showLocationDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Location")
                }
            }

            // Show filtered locations if doctor is selected, otherwise show all
            val locationsToShow = if (selectedDoctorId != null) doctorLocations else locations

            if (locationsToShow.isEmpty()) {
                if (selectedDoctorId != null) {
                    Text(
                        "No locations linked to this doctor yet. Add one using the + button above.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "No locations yet. Add one using the + button above.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                // Sort locations by distance (closest first)
                val sortedLocations = locationsToShow.sortedBy { it.distanceMiles ?: Double.MAX_VALUE }

                sortedLocations.forEach { loc ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedLocationId == loc.id,
                            onClick = { selectedLocationId = loc.id }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = loc.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (loc.distanceMiles != null) {
                                Text(
                                    text = LocationUtils.formatDistance(loc.distanceMiles),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (!loc.address.isNullOrEmpty()) {
                                Text(
                                    text = loc.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Option to deselect location
            if (selectedLocationId != null) {
                TextButton(onClick = { selectedLocationId = null }) {
                    Text("Clear Location")
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Enable Reminder")
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = { reminderEnabled = it }
                )
            }

            if (reminderEnabled) {
                OutlinedTextField(
                    value = reminderMinutes,
                    onValueChange = { reminderMinutes = it.filter { char -> char.isDigit() } },
                    label = { Text("Minutes Before") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    selectedProfileId?.let { profileId ->
                        if (title.isNotBlank()) {
                            val reminderMin = reminderMinutes.toIntOrNull() ?: 60
                            if (existingAppointment != null) {
                                viewModel.updateAppointment(
                                    existingAppointment.copy(
                                        title = title.trim(),
                                        dateTime = dateTime,
                                        location = location.trim().takeIf { it.isNotEmpty() },
                                        doctorId = selectedDoctorId,
                                        locationId = selectedLocationId,
                                        notes = notes.trim().takeIf { it.isNotEmpty() },
                                        reminderEnabled = reminderEnabled,
                                        reminderMinutesBefore = reminderMin
                                    )
                                )
                            } else {
                                viewModel.addAppointment(
                                    Appointment(
                                        profileId = profileId,
                                        title = title.trim(),
                                        dateTime = dateTime,
                                        location = location.trim().takeIf { it.isNotEmpty() },
                                        doctorId = selectedDoctorId,
                                        locationId = selectedLocationId,
                                        notes = notes.trim().takeIf { it.isNotEmpty() },
                                        reminderEnabled = reminderEnabled,
                                        reminderMinutesBefore = reminderMin
                                    )
                                )
                            }
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateTime
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate ->
                        // Extract date components from UTC
                        val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = selectedDate
                        }
                        // Apply to local calendar while preserving time
                        val newCalendar = Calendar.getInstance().apply {
                            timeInMillis = dateTime
                            set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
                            set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
                        }
                        dateTime = newCalendar.timeInMillis
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    val newCalendar = Calendar.getInstance().apply {
                        timeInMillis = dateTime
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                    }
                    dateTime = newCalendar.timeInMillis
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog to add new doctor
    if (showDoctorDialog) {
        AlertDialog(
            onDismissRequest = { showDoctorDialog = false },
            title = { Text("Add Doctor") },
            text = {
                OutlinedTextField(
                    value = newDoctorName,
                    onValueChange = { newDoctorName = it },
                    label = { Text("Doctor Name") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newDoctorName.isNotBlank()) {
                            viewModel.addDoctor(
                                Doctor(name = newDoctorName.trim())
                            )
                            newDoctorName = ""
                            showDoctorDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDoctorDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog to add new location
    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Add Location") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newLocationName,
                        onValueChange = { newLocationName = it },
                        label = { Text("Location Name *") },
                        placeholder = { Text("Dr. Kia - Plano Office") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                    OutlinedTextField(
                        value = newLocationAddress,
                        onValueChange = { newLocationAddress = it },
                        label = { Text("Address *") },
                        placeholder = { Text("123 Main St, Plano, TX") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                    OutlinedTextField(
                        value = newLocationPhone,
                        onValueChange = { newLocationPhone = it },
                        label = { Text("Phone") },
                        placeholder = { Text("(555) 123-4567") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (geocodingNewLocation) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Calculating distance...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newLocationName.isNotBlank() && newLocationAddress.isNotBlank()) {
                            geocodingNewLocation = true
                            scope.launch {
                                // Geocode the address
                                val coords = LocationUtils.geocodeAddress(context, newLocationAddress)

                                // Calculate distance from home
                                val distance = if (coords != null && currentProfile != null) {
                                    LocationUtils.calculateDistanceFromHome(
                                        currentProfile!!.homeLatitude,
                                        currentProfile!!.homeLongitude,
                                        coords.first,
                                        coords.second
                                    )
                                } else {
                                    null
                                }

                                // Create location
                                val newLocation = Location(
                                    name = newLocationName.trim(),
                                    address = newLocationAddress.trim(),
                                    latitude = coords?.first,
                                    longitude = coords?.second,
                                    distanceMiles = distance,
                                    phone = newLocationPhone.trim().takeIf { it.isNotEmpty() }
                                )

                                viewModel.addLocation(newLocation)

                                // Link to selected doctor if one is selected
                                if (selectedDoctorId != null) {
                                    // Wait a bit for location to be inserted
                                    kotlinx.coroutines.delay(100)
                                    // Link doctor to location (we'll get the new location ID from the locations list)
                                    // Note: This is a simplified approach. In production, you'd want to get the actual inserted ID
                                }

                                // Reset dialog state
                                newLocationName = ""
                                newLocationAddress = ""
                                newLocationPhone = ""
                                geocodingNewLocation = false
                                showLocationDialog = false
                            }
                        }
                    },
                    enabled = newLocationName.isNotBlank() && newLocationAddress.isNotBlank() && !geocodingNewLocation
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    newLocationName = ""
                    newLocationAddress = ""
                    newLocationPhone = ""
                    showLocationDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
