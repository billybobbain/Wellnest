package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.Medication
import com.billybobbain.wellnest.utils.MedicationImporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationsScreen(
    viewModel: WellnestViewModel,
    onNavigateBack: () -> Unit,
    onAddMedication: () -> Unit,
    onEditMedication: (Long) -> Unit
) {
    val medications by viewModel.medications.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    var showImportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medications") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Share button
                    IconButton(
                        onClick = {
                            val shareText = formatMedicationsForSharing(medications, currentProfile?.name ?: "Patient")
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                putExtra(Intent.EXTRA_SUBJECT, "Medication List")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Medications"))
                        },
                        enabled = medications.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Medications")
                    }
                    // Import button
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Import from CSV")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMedication) {
                Icon(Icons.Default.Add, contentDescription = "Add Medication")
            }
        }
    ) { padding ->
        if (medications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No medications yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(medications) { medication ->
                    MedicationCard(
                        medication = medication,
                        onClick = { onEditMedication(medication.id) },
                        onDelete = { viewModel.deleteMedication(medication) }
                    )
                }
            }
        }
    }

    // Import Dialog
    if (showImportDialog) {
        ImportMedicationsDialog(
            viewModel = viewModel,
            onDismiss = { showImportDialog = false }
        )
    }
}

@Composable
fun ImportMedicationsDialog(
    viewModel: WellnestViewModel,
    onDismiss: () -> Unit
) {
    var csvText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val currentProfile by viewModel.currentProfile.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Medications from CSV") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Paste your CSV data below:",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Expected format:\nMedication,Strength,Type,Route,Frequency,Instruction,Indication,Schedule,Prescriber,Date",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = csvText,
                    onValueChange = {
                        csvText = it
                        errorMessage = null
                        successMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = { Text("Paste CSV here...") },
                    maxLines = 10
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (successMessage != null) {
                    Text(
                        text = successMessage!!,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentProfile == null) {
                        errorMessage = "No profile selected"
                        return@Button
                    }

                    if (csvText.isBlank()) {
                        errorMessage = "Please paste CSV data"
                        return@Button
                    }

                    val result = MedicationImporter.parseCsv(csvText, currentProfile!!.id)
                    result.fold(
                        onSuccess = { medications ->
                            if (medications.isEmpty()) {
                                errorMessage = "No valid medications found in CSV"
                            } else {
                                // Import medications
                                medications.forEach { medication ->
                                    viewModel.addMedication(medication)
                                }
                                successMessage = "Successfully imported ${medications.size} medication(s)"
                                // Clear the text field
                                csvText = ""
                            }
                        },
                        onFailure = { exception ->
                            errorMessage = "Error: ${exception.message}"
                        }
                    )
                },
                enabled = csvText.isNotBlank() && currentProfile != null
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationCard(
    medication: Medication,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                    text = medication.drugName,
                    style = MaterialTheme.typography.titleMedium
                )
                if (!medication.classification.isNullOrEmpty() || !medication.diagnosis.isNullOrEmpty()) {
                    Text(
                        text = listOfNotNull(medication.classification, medication.diagnosis)
                            .joinToString(" - "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (!medication.dosage.isNullOrEmpty() || !medication.frequency.isNullOrEmpty()) {
                    Text(
                        text = listOfNotNull(medication.dosage, medication.frequency)
                            .joinToString(" - "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!medication.prescribingDoctor.isNullOrEmpty()) {
                    Text(
                        text = "Dr. ${medication.prescribingDoctor}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

/**
 * Format medications list for sharing via text/email
 */
private fun formatMedicationsForSharing(medications: List<Medication>, profileName: String): String {
    if (medications.isEmpty()) {
        return "No medications to share"
    }

    val dateFormat = java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.US)

    return buildString {
        appendLine("MEDICATION LIST")
        appendLine("Patient: $profileName")
        appendLine("Generated: ${dateFormat.format(java.util.Date())}")
        appendLine()
        appendLine("=".repeat(40))
        appendLine()

        medications.forEachIndexed { index, med ->
            appendLine("${index + 1}. ${med.drugName}")

            if (!med.dosage.isNullOrEmpty()) {
                appendLine("   Dosage: ${med.dosage}")
            }

            if (!med.frequency.isNullOrEmpty()) {
                appendLine("   Frequency: ${med.frequency}")
            }

            if (!med.diagnosis.isNullOrEmpty()) {
                appendLine("   For: ${med.diagnosis}")
            }

            if (!med.prescribingDoctor.isNullOrEmpty()) {
                appendLine("   Prescribed by: ${med.prescribingDoctor}")
            }

            if (med.startDate != null) {
                appendLine("   Started: ${dateFormat.format(java.util.Date(med.startDate))}")
            }

            if (!med.notes.isNullOrEmpty()) {
                appendLine("   Notes: ${med.notes}")
            }

            if (!med.classification.isNullOrEmpty()) {
                appendLine("   Classification: ${med.classification}")
            }

            if (!med.pharmacy.isNullOrEmpty()) {
                appendLine("   Pharmacy: ${med.pharmacy}")
            }

            appendLine()
        }

        appendLine("=".repeat(40))
        appendLine("Total: ${medications.size} medication(s)")
    }
}
