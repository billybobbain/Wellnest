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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.Appointment
import com.billybobbain.wellnest.data.Doctor
import com.billybobbain.wellnest.data.Location
import com.billybobbain.wellnest.data.RecurringAppointment
import com.billybobbain.wellnest.utils.LocationUtils
import com.billybobbain.wellnest.utils.RecurringAppointmentExpander
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAppointmentScreen(
    viewModel: WellnestViewModel,
    appointmentId: Long?,
    onNavigateBack: () -> Unit
) {
    val appointments by viewModel.appointments.collectAsState()
    val recurringAppointments by viewModel.recurringAppointments.collectAsState()
    val doctors by viewModel.doctors.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    val selectedProfileId by viewModel.selectedProfileId.collectAsState()

    // Check if this is editing a recurring appointment
    // Could be: 1) Virtual instance (negative ID), 2) Direct recurring appointment (positive ID from recurring list)
    val isVirtualInstance = appointmentId != null && RecurringAppointmentExpander.isVirtualInstance(appointmentId)
    val recurringId = if (isVirtualInstance) {
        RecurringAppointmentExpander.getRecurringIdFromVirtual(appointmentId!!)
    } else {
        appointmentId  // Might be a direct recurring appointment ID
    }

    // Try to find existing recurring appointment
    val existingRecurring = recurringAppointments.find { it.id == recurringId }

    // Only look for regular appointment if it's not a recurring one
    val existingAppointment = if (existingRecurring == null && !isVirtualInstance) {
        appointments.find { it.id == appointmentId }
    } else {
        null
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf(existingRecurring?.title ?: existingAppointment?.title ?: "") }
    var selectedDoctorId by remember { mutableStateOf(existingRecurring?.doctorId ?: existingAppointment?.doctorId) }
    var selectedLocationId by remember { mutableStateOf(existingRecurring?.locationId ?: existingAppointment?.locationId) }
    var notes by remember { mutableStateOf(existingRecurring?.notes ?: existingAppointment?.notes ?: "") }
    var dateTime by remember {
        mutableStateOf(
            when {
                existingAppointment != null -> existingAppointment.dateTime
                existingRecurring != null -> {
                    // Convert timeOfDay (milliseconds since midnight) to a full dateTime
                    val cal = Calendar.getInstance()
                    val hours = (existingRecurring.timeOfDay / 3_600_000).toInt()
                    val minutes = ((existingRecurring.timeOfDay % 3_600_000) / 60_000).toInt()
                    cal.set(Calendar.HOUR_OF_DAY, hours)
                    cal.set(Calendar.MINUTE, minutes)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    cal.timeInMillis
                }
                else -> System.currentTimeMillis()
            }
        )
    }
    var reminderEnabled by remember { mutableStateOf(existingRecurring?.reminderEnabled ?: existingAppointment?.reminderEnabled ?: false) }
    var reminderMinutes by remember { mutableStateOf((existingRecurring?.reminderMinutesBefore ?: existingAppointment?.reminderMinutesBefore ?: 60).toString()) }
    var selectedIcon by remember { mutableStateOf(existingRecurring?.icon ?: existingAppointment?.icon ?: "📋") }

    // Recurring appointment state
    var isRecurring by remember { mutableStateOf(existingRecurring != null) }
    var selectedDays by remember {
        mutableStateOf(
            existingRecurring?.daysOfWeek?.split(",")?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDoctorDialog by remember { mutableStateOf(false) }
    var newDoctorName by remember { mutableStateOf("") }
    var showLocationDialog by remember { mutableStateOf(false) }
    var newLocationName by remember { mutableStateOf("") }
    var newLocationAddress by remember { mutableStateOf("") }
    var newLocationPhone by remember { mutableStateOf("") }
    var geocodingNewLocation by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }

    // Track locations for selected doctor
    // Use the main locations list as a key so it refreshes when locations are added
    val doctorLocations by produceState<List<Location>>(
        initialValue = emptyList(),
        key1 = selectedDoctorId,
        key2 = locations.size  // Refresh when locations are added/removed
    ) {
        value = if (selectedDoctorId != null) {
            viewModel.repository.getLocationsForDoctor(selectedDoctorId!!).first()
        } else {
            emptyList()
        }
    }

    // Clear location selection if it's not valid for the newly selected doctor
    // BUT: Only do this when creating new appointments, not when editing existing ones
    LaunchedEffect(selectedDoctorId, doctorLocations) {
        if (appointmentId == null) {  // Only for new appointments
            if (selectedDoctorId != null && selectedLocationId != null) {
                if (!doctorLocations.any { it.id == selectedLocationId }) {
                    selectedLocationId = null
                }
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

            // Icon picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Icon", style = MaterialTheme.typography.titleSmall)
                OutlinedButton(
                    onClick = { showIconPicker = true }
                ) {
                    Text(
                        text = selectedIcon,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Text("Date & Time", style = MaterialTheme.typography.titleSmall)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f),
                    enabled = !isRecurring  // Disable date picker for recurring appointments
                ) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text("Date", style = MaterialTheme.typography.labelSmall)
                        Text(if (isRecurring) "---" else dateFormat.format(Date(dateTime)))
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

            // Recurring toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Repeats Weekly", style = MaterialTheme.typography.titleSmall)
                Switch(
                    checked = isRecurring,
                    onCheckedChange = { isRecurring = it },
                    enabled = existingAppointment == null && existingRecurring == null  // Can only toggle when creating new
                )
            }

            // Day selector for recurring appointments
            if (isRecurring) {
                Text("Repeat On", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEachIndexed { index, day ->
                        val dayValue = index + 1  // 1=Sunday, 7=Saturday
                        FilterChip(
                            selected = dayValue in selectedDays,
                            onClick = {
                                selectedDays = if (dayValue in selectedDays) {
                                    selectedDays - dayValue
                                } else {
                                    selectedDays + dayValue
                                }
                            },
                            label = { Text(day, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
                if (selectedDays.isEmpty()) {
                    Text(
                        "Select at least one day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

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

                            if (isRecurring) {
                                // Save as recurring appointment
                                if (selectedDays.isNotEmpty()) {
                                    // Calculate time of day in milliseconds since midnight
                                    val cal = Calendar.getInstance().apply { timeInMillis = dateTime }
                                    val timeOfDay = LocalTime.of(
                                        cal.get(Calendar.HOUR_OF_DAY),
                                        cal.get(Calendar.MINUTE)
                                    ).toNanoOfDay() / 1_000_000

                                    val daysOfWeekString = selectedDays.sorted().joinToString(",")

                                    if (existingRecurring != null) {
                                        viewModel.updateRecurringAppointment(
                                            existingRecurring.copy(
                                                title = title.trim(),
                                                timeOfDay = timeOfDay,
                                                daysOfWeek = daysOfWeekString,
                                                location = null,  // Legacy field - now using locationId
                                                doctorId = selectedDoctorId,
                                                locationId = selectedLocationId,
                                                notes = notes.trim().takeIf { it.isNotEmpty() },
                                                reminderEnabled = reminderEnabled,
                                                reminderMinutesBefore = reminderMin,
                                                icon = selectedIcon
                                            )
                                        )
                                    } else {
                                        viewModel.addRecurringAppointment(
                                            RecurringAppointment(
                                                profileId = profileId,
                                                title = title.trim(),
                                                timeOfDay = timeOfDay,
                                                daysOfWeek = daysOfWeekString,
                                                location = null,  // Legacy field - now using locationId
                                                doctorId = selectedDoctorId,
                                                locationId = selectedLocationId,
                                                notes = notes.trim().takeIf { it.isNotEmpty() },
                                                reminderEnabled = reminderEnabled,
                                                reminderMinutesBefore = reminderMin,
                                                icon = selectedIcon
                                            )
                                        )
                                    }
                                    onNavigateBack()
                                }
                            } else {
                                // Save as one-time appointment
                                if (existingAppointment != null) {
                                    viewModel.updateAppointment(
                                        existingAppointment.copy(
                                            title = title.trim(),
                                            dateTime = dateTime,
                                            location = null,  // Legacy field - now using locationId
                                            doctorId = selectedDoctorId,
                                            locationId = selectedLocationId,
                                            notes = notes.trim().takeIf { it.isNotEmpty() },
                                            reminderEnabled = reminderEnabled,
                                            reminderMinutesBefore = reminderMin,
                                            icon = selectedIcon
                                        )
                                    )
                                } else {
                                    viewModel.addAppointment(
                                        Appointment(
                                            profileId = profileId,
                                            title = title.trim(),
                                            dateTime = dateTime,
                                            location = null,  // Legacy field - now using locationId
                                            doctorId = selectedDoctorId,
                                            locationId = selectedLocationId,
                                            notes = notes.trim().takeIf { it.isNotEmpty() },
                                            reminderEnabled = reminderEnabled,
                                            reminderMinutesBefore = reminderMin,
                                            icon = selectedIcon
                                        )
                                    )
                                }
                                onNavigateBack()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && (!isRecurring || selectedDays.isNotEmpty())
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

                                // Insert location and get the new ID
                                val newLocationId = viewModel.repository.insertLocation(newLocation)

                                // Link to selected doctor if one is selected
                                if (selectedDoctorId != null) {
                                    viewModel.linkDoctorToLocation(selectedDoctorId!!, newLocationId)
                                }

                                // Wait a moment for the location to appear in the StateFlow
                                kotlinx.coroutines.delay(200)

                                // Auto-select the newly created location
                                selectedLocationId = newLocationId

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

    // Icon picker dialog
    if (showIconPicker) {
        val icons = listOf(
            "🏥" to "Doctor",
            "🦷" to "Dentist",
            "👁️" to "Eye Doctor",
            "💉" to "Lab/Blood",
            "💊" to "Pharmacy",
            "🧠" to "Therapy",
            "🩺" to "Checkup",
            "💇‍♀️" to "Hair",
            "💅" to "Nails",
            "💆‍♀️" to "Spa",
            "🎉" to "Bingo",
            "📞" to "Call",
            "🍽️" to "Meal",
            "🎨" to "Activity",
            "🏃‍♀️" to "Exercise",
            "📋" to "General",
            "⭐" to "Important",
            "🏠" to "Home Visit"
        )

        AlertDialog(
            onDismissRequest = { showIconPicker = false },
            title = { Text("Choose Icon") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    icons.chunked(4).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { (icon, label) ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable {
                                            selectedIcon = icon
                                            showIconPicker = false
                                        }
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = icon,
                                        style = MaterialTheme.typography.displaySmall
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIconPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
