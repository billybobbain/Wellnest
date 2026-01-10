package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.HealthProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthProfileScreen(
    viewModel: WellnestViewModel,
    onNavigateBack: () -> Unit
) {
    val healthProfile by viewModel.healthProfile.collectAsState()
    val selectedProfileId by viewModel.selectedProfileId.collectAsState()

    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var medicalConditions by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }
    var emergencyPhone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Update state when healthProfile loads
    LaunchedEffect(healthProfile) {
        healthProfile?.let { profile ->
            height = profile.height ?: ""
            weight = profile.weight ?: ""
            bloodType = profile.bloodType ?: ""
            allergies = profile.allergies ?: ""
            medicalConditions = profile.medicalConditions ?: ""
            emergencyContact = profile.emergencyContact ?: ""
            emergencyPhone = profile.emergencyPhone ?: ""
            notes = profile.notes ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Profile") },
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
            Text("Basic Information", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Height") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., 5'6\"") }
            )

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., 150 lbs") }
            )

            OutlinedTextField(
                value = bloodType,
                onValueChange = { bloodType = it },
                label = { Text("Blood Type") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., O+") }
            )

            Divider()

            Text("Medical Information", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = allergies,
                onValueChange = { allergies = it },
                label = { Text("Allergies") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = medicalConditions,
                onValueChange = { medicalConditions = it },
                label = { Text("Medical Conditions") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Divider()

            Text("Emergency Contact", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = emergencyContact,
                onValueChange = { emergencyContact = it },
                label = { Text("Emergency Contact Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = emergencyPhone,
                onValueChange = { emergencyPhone = it },
                label = { Text("Emergency Phone") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Additional Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {
                    selectedProfileId?.let { profileId ->
                        viewModel.addOrUpdateHealthProfile(
                            HealthProfile(
                                profileId = profileId,
                                height = height.trim().takeIf { it.isNotEmpty() },
                                weight = weight.trim().takeIf { it.isNotEmpty() },
                                bloodType = bloodType.trim().takeIf { it.isNotEmpty() },
                                allergies = allergies.trim().takeIf { it.isNotEmpty() },
                                medicalConditions = medicalConditions.trim().takeIf { it.isNotEmpty() },
                                emergencyContact = emergencyContact.trim().takeIf { it.isNotEmpty() },
                                emergencyPhone = emergencyPhone.trim().takeIf { it.isNotEmpty() },
                                notes = notes.trim().takeIf { it.isNotEmpty() }
                            )
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
