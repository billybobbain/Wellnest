package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.Doctor
import com.billybobbain.wellnest.utils.LocationUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDoctorScreen(
    viewModel: WellnestViewModel,
    doctorId: Long?,
    onNavigateBack: () -> Unit
) {
    val doctors by viewModel.doctors.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val scope = rememberCoroutineScope()
    val existingDoctor = doctors.find { it.id == doctorId }

    // Track which locations are linked to this doctor
    val linkedLocations = remember { mutableStateListOf<Long>() }

    // Load linked locations when editing existing doctor
    LaunchedEffect(doctorId) {
        if (doctorId != null) {
            viewModel.repository.getLocationsForDoctor(doctorId).collect { locationsList ->
                linkedLocations.clear()
                linkedLocations.addAll(locationsList.map { it.id })
            }
        }
    }

    var name by remember { mutableStateOf(existingDoctor?.name ?: "") }
    var specialty by remember { mutableStateOf(existingDoctor?.specialty ?: "") }
    var phone by remember { mutableStateOf(existingDoctor?.phone ?: "") }
    var notes by remember { mutableStateOf(existingDoctor?.notes ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (doctorId == null) "Add Doctor" else "Edit Doctor") },
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
                label = { Text("Doctor Name *") },
                placeholder = { Text("Dr. Smith") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            OutlinedTextField(
                value = specialty,
                onValueChange = { specialty = it },
                label = { Text("Specialty") },
                placeholder = { Text("Cardiology") },
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
                text = "Practice Locations",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (locations.isEmpty()) {
                Text(
                    text = "No locations in system yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Sort locations by distance
                val sortedLocations = locations.sortedBy { it.distanceMiles ?: Double.MAX_VALUE }

                sortedLocations.forEach { location ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = linkedLocations.contains(location.id),
                            onCheckedChange = { checked ->
                                if (checked) {
                                    linkedLocations.add(location.id)
                                } else {
                                    linkedLocations.remove(location.id)
                                }
                            }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = location.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (location.distanceMiles != null) {
                                Text(
                                    text = LocationUtils.formatDistance(location.distanceMiles),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = location.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        scope.launch {
                            val doctor = Doctor(
                                id = doctorId ?: 0,
                                name = name.trim(),
                                specialty = specialty.trim().takeIf { it.isNotEmpty() },
                                phone = phone.trim().takeIf { it.isNotEmpty() },
                                notes = notes.trim().takeIf { it.isNotEmpty() }
                            )

                            val savedDoctorId = if (existingDoctor != null) {
                                viewModel.repository.updateDoctor(doctor)
                                existingDoctor.id
                            } else {
                                viewModel.repository.insertDoctor(doctor)
                            }

                            // Update doctor-location links
                            // First, get current links
                            val currentLinks = viewModel.repository.getLocationsForDoctor(savedDoctorId)
                                .first()
                                .map { it.id }
                                .toSet()

                            // Remove unlinked locations
                            currentLinks.forEach { locationId ->
                                if (!linkedLocations.contains(locationId)) {
                                    viewModel.repository.unlinkDoctorFromLocation(savedDoctorId, locationId)
                                }
                            }

                            // Add newly linked locations
                            linkedLocations.forEach { locationId ->
                                if (!currentLinks.contains(locationId)) {
                                    viewModel.repository.linkDoctorToLocation(savedDoctorId, locationId)
                                }
                            }

                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}
