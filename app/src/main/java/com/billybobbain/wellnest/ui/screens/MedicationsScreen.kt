package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.Medication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationsScreen(
    viewModel: WellnestViewModel,
    onNavigateBack: () -> Unit,
    onAddMedication: () -> Unit,
    onEditMedication: (Long) -> Unit
) {
    val medications by viewModel.medications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medications") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
