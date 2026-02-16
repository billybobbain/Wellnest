package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.Doctor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorsScreen(
    viewModel: WellnestViewModel,
    onNavigateBack: () -> Unit,
    onAddDoctor: () -> Unit,
    onEditDoctor: (Long) -> Unit
) {
    val doctors by viewModel.doctors.collectAsState()
    val locations by viewModel.locations.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doctors") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAddDoctor) {
                        Icon(Icons.Default.Add, contentDescription = "Add Doctor")
                    }
                }
            )
        }
    ) { padding ->
        if (doctors.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No doctors yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap + to add your first doctor",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sort alphabetically by name
                val sortedDoctors = doctors.sortedBy { it.name }

                items(sortedDoctors) { doctor ->
                    DoctorCard(
                        doctor = doctor,
                        viewModel = viewModel,
                        onClick = { onEditDoctor(doctor.id) },
                        onDelete = { viewModel.deleteDoctor(doctor) }
                    )
                }
            }
        }
    }
}

@Composable
fun DoctorCard(
    doctor: Doctor,
    viewModel: WellnestViewModel,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    // Get locations for this doctor
    val locationsForDoctor by viewModel.repository.getLocationsForDoctor(doctor.id).collectAsState(initial = emptyList())

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doctor.name,
                    style = MaterialTheme.typography.titleMedium
                )

                if (!doctor.specialty.isNullOrEmpty()) {
                    Text(
                        text = doctor.specialty,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Show address (first line only if it has commas)
                if (!doctor.address.isNullOrEmpty()) {
                    val addressFirstLine = doctor.address.split(",").firstOrNull()?.trim() ?: doctor.address
                    Text(
                        text = addressFirstLine,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!doctor.phone.isNullOrEmpty()) {
                    Text(
                        text = doctor.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (locationsForDoctor.isNotEmpty()) {
                    Text(
                        text = "Locations: ${locationsForDoctor.joinToString(", ") { it.name }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
