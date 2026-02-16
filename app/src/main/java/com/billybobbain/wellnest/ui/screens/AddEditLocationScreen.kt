package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.Location
import com.billybobbain.wellnest.utils.LocationUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLocationScreen(
    viewModel: WellnestViewModel,
    locationId: Long?,
    onNavigateBack: () -> Unit
) {
    val locations by viewModel.locations.collectAsState()
    val doctors by viewModel.doctors.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val existingLocation = locations.find { it.id == locationId }

    // Track which doctors are linked to this location
    val linkedDoctors = remember { mutableStateListOf<Long>() }

    // Load linked doctors when editing existing location
    LaunchedEffect(locationId) {
        if (locationId != null) {
            viewModel.repository.getDoctorsForLocation(locationId).collect { doctorsList ->
                linkedDoctors.clear()
                linkedDoctors.addAll(doctorsList.map { it.id })
            }
        }
    }

    var name by remember { mutableStateOf(existingLocation?.name ?: "") }
    var address by remember { mutableStateOf(existingLocation?.address ?: "") }
    var phone by remember { mutableStateOf(existingLocation?.phone ?: "") }
    var notes by remember { mutableStateOf(existingLocation?.notes ?: "") }
    var latitude by remember { mutableStateOf(existingLocation?.latitude) }
    var longitude by remember { mutableStateOf(existingLocation?.longitude) }
    var distanceMiles by remember { mutableStateOf(existingLocation?.distanceMiles) }
    var geocoding by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (locationId == null) "Add Location" else "Edit Location") },
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
                value = name,
                onValueChange = { name = it },
                label = { Text("Location Name *") },
                placeholder = { Text("Dr. Kia - Plano Office") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address *") },
                placeholder = { Text("123 Main St, Plano, TX 75024") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                placeholder = { Text("(555) 123-4567") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                placeholder = { Text("Additional details...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            HorizontalDivider()

            Text(
                text = "Coordinates & Distance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (latitude != null && longitude != null) {
                Text(
                    text = "Lat: ${String.format("%.4f", latitude)}, Lng: ${String.format("%.4f", longitude)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (distanceMiles != null) {
                Text(
                    text = "Distance from home: ${LocationUtils.formatDistance(distanceMiles)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = {
                    if (address.isNotBlank()) {
                        geocoding = true
                        scope.launch {
                            val coords = LocationUtils.geocodeAddress(context, address)
                            if (coords != null) {
                                latitude = coords.first
                                longitude = coords.second

                                // Calculate distance from home
                                distanceMiles = LocationUtils.calculateDistanceFromHome(
                                    currentProfile?.homeLatitude,
                                    currentProfile?.homeLongitude,
                                    coords.first,
                                    coords.second
                                )
                            }
                            geocoding = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = address.isNotBlank() && !geocoding
            ) {
                if (geocoding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (geocoding) "Calculating..." else "Calculate Distance from Address")
            }

            HorizontalDivider()

            Text(
                text = "Doctors at This Location",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (doctors.isEmpty()) {
                Text(
                    text = "No doctors in system yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                doctors.forEach { doctor ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = linkedDoctors.contains(doctor.id),
                            onCheckedChange = { checked ->
                                if (checked) {
                                    linkedDoctors.add(doctor.id)
                                } else {
                                    linkedDoctors.remove(doctor.id)
                                }
                            }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = doctor.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (!doctor.specialty.isNullOrEmpty()) {
                                Text(
                                    text = doctor.specialty,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (name.isNotBlank() && address.isNotBlank()) {
                        scope.launch {
                            val location = Location(
                                id = locationId ?: 0,
                                name = name.trim(),
                                address = address.trim(),
                                latitude = latitude,
                                longitude = longitude,
                                distanceMiles = distanceMiles,
                                phone = phone.trim().takeIf { it.isNotEmpty() },
                                notes = notes.trim().takeIf { it.isNotEmpty() }
                            )

                            val savedLocationId = if (existingLocation != null) {
                                viewModel.repository.updateLocation(location)
                                existingLocation.id
                            } else {
                                viewModel.repository.insertLocation(location)
                            }

                            // Update doctor-location links
                            // First, get current links
                            val currentLinks = viewModel.repository.getDoctorsForLocation(savedLocationId)
                                .first()
                                .map { it.id }
                                .toSet()

                            // Remove unlinked doctors
                            currentLinks.forEach { doctorId ->
                                if (!linkedDoctors.contains(doctorId)) {
                                    viewModel.repository.unlinkDoctorFromLocation(doctorId, savedLocationId)
                                }
                            }

                            // Add newly linked doctors
                            linkedDoctors.forEach { doctorId ->
                                if (!currentLinks.contains(doctorId)) {
                                    viewModel.repository.linkDoctorToLocation(doctorId, savedLocationId)
                                }
                            }

                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && address.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}
