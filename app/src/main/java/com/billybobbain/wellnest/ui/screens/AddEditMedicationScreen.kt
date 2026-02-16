package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.Doctor
import com.billybobbain.wellnest.data.Medication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicationScreen(
    viewModel: WellnestViewModel,
    medicationId: Long?,
    onNavigateBack: () -> Unit
) {
    val medications by viewModel.medications.collectAsState()
    val doctors by viewModel.doctors.collectAsState()
    val selectedProfileId by viewModel.selectedProfileId.collectAsState()
    val existingMedication = medications.find { it.id == medicationId }

    var drugName by remember { mutableStateOf(existingMedication?.drugName ?: "") }
    var dosage by remember { mutableStateOf(existingMedication?.dosage ?: "") }
    var frequency by remember { mutableStateOf(existingMedication?.frequency ?: "") }
    var selectedDoctorId by remember { mutableStateOf(existingMedication?.doctorId) }
    var pharmacy by remember { mutableStateOf(existingMedication?.pharmacy ?: "") }
    var notes by remember { mutableStateOf(existingMedication?.notes ?: "") }
    var classification by remember { mutableStateOf(existingMedication?.classification ?: "") }
    var diagnosis by remember { mutableStateOf(existingMedication?.diagnosis ?: "") }

    var classificationExpanded by remember { mutableStateOf(false) }
    var diagnosisExpanded by remember { mutableStateOf(false) }
    var showDoctorDialog by remember { mutableStateOf(false) }
    var newDoctorName by remember { mutableStateOf("") }

    val classificationOptions = listOf(
        "Antianxiety",
        "Antidepressant",
        "Antihypertensive",
        "Calcium Channel Blocker",
        "Antacids",
        "Antiviral",
        "Ophthalmic Agent"
    )

    val diagnosisOptions = listOf(
        "Hypertension",
        "Depression",
        "Dry Eyes",
        "GERD",
        "HIV",
        "Anxiety"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (medicationId == null) "Add Medication" else "Edit Medication") },
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
                value = drugName,
                onValueChange = { drugName = it },
                label = { Text("Drug Name *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., 10mg") }
            )

            OutlinedTextField(
                value = frequency,
                onValueChange = { frequency = it },
                label = { Text("Frequency") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Twice daily") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            // Doctor selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Prescribing Doctor", style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = { showDoctorDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Doctor")
                }
            }

            if (doctors.isEmpty()) {
                Text("No doctors yet. Add one using the + button above.")
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

            OutlinedTextField(
                value = pharmacy,
                onValueChange = { pharmacy = it },
                label = { Text("Pharmacy") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            ExposedDropdownMenuBox(
                expanded = classificationExpanded,
                onExpandedChange = { classificationExpanded = it }
            ) {
                OutlinedTextField(
                    value = classification,
                    onValueChange = { classification = it },
                    label = { Text("Classification") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classificationExpanded) },
                    placeholder = { Text("e.g., Antianxiety") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                ExposedDropdownMenu(
                    expanded = classificationExpanded,
                    onDismissRequest = { classificationExpanded = false }
                ) {
                    classificationOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                classification = option
                                classificationExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = diagnosisExpanded,
                onExpandedChange = { diagnosisExpanded = it }
            ) {
                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    label = { Text("Diagnosis") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diagnosisExpanded) },
                    placeholder = { Text("e.g., Hypertension") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                ExposedDropdownMenu(
                    expanded = diagnosisExpanded,
                    onDismissRequest = { diagnosisExpanded = false }
                ) {
                    diagnosisOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                diagnosis = option
                                diagnosisExpanded = false
                            }
                        )
                    }
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

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    selectedProfileId?.let { profileId ->
                        if (drugName.isNotBlank()) {
                            if (existingMedication != null) {
                                viewModel.updateMedication(
                                    existingMedication.copy(
                                        drugName = drugName.trim(),
                                        dosage = dosage.trim().takeIf { it.isNotEmpty() },
                                        frequency = frequency.trim().takeIf { it.isNotEmpty() },
                                        doctorId = selectedDoctorId,
                                        pharmacy = pharmacy.trim().takeIf { it.isNotEmpty() },
                                        notes = notes.trim().takeIf { it.isNotEmpty() },
                                        classification = classification.trim().takeIf { it.isNotEmpty() },
                                        diagnosis = diagnosis.trim().takeIf { it.isNotEmpty() }
                                    )
                                )
                            } else {
                                viewModel.addMedication(
                                    Medication(
                                        profileId = profileId,
                                        drugName = drugName.trim(),
                                        dosage = dosage.trim().takeIf { it.isNotEmpty() },
                                        frequency = frequency.trim().takeIf { it.isNotEmpty() },
                                        doctorId = selectedDoctorId,
                                        pharmacy = pharmacy.trim().takeIf { it.isNotEmpty() },
                                        notes = notes.trim().takeIf { it.isNotEmpty() },
                                        classification = classification.trim().takeIf { it.isNotEmpty() },
                                        diagnosis = diagnosis.trim().takeIf { it.isNotEmpty() }
                                    )
                                )
                            }
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = drugName.isNotBlank()
            ) {
                Text("Save")
            }
        }
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
}
