package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.InsurancePolicy
import com.billybobbain.wellnest.data.InsuranceProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditInsurancePolicyScreen(
    viewModel: WellnestViewModel,
    policyId: Long?,
    onNavigateBack: () -> Unit
) {
    val policies by viewModel.insurancePolicies.collectAsState()
    val providers by viewModel.insuranceProviders.collectAsState()
    val selectedProfileId by viewModel.selectedProfileId.collectAsState()
    val existingPolicy = policies.find { it.id == policyId }

    var selectedProviderId by remember { mutableStateOf(existingPolicy?.providerId) }
    var policyNumber by remember { mutableStateOf(existingPolicy?.policyNumber ?: "") }
    var memberPhone by remember { mutableStateOf(existingPolicy?.memberPhone ?: "") }
    var providerPhone by remember { mutableStateOf(existingPolicy?.providerPhone ?: "") }
    var coverageType by remember { mutableStateOf(existingPolicy?.coverageType ?: "") }
    var insuranceType by remember { mutableStateOf(existingPolicy?.insuranceType ?: "") }
    var notes by remember { mutableStateOf(existingPolicy?.notes ?: "") }
    var showProviderDialog by remember { mutableStateOf(false) }
    var newProviderName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (policyId == null) "Add Insurance" else "Edit Insurance") },
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
            // Provider selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Insurance Provider *", style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = { showProviderDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Provider")
                }
            }

            if (providers.isEmpty()) {
                Text("No providers yet. Add one using the + button above.")
            } else {
                providers.forEach { provider ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RadioButton(
                            selected = selectedProviderId == provider.id,
                            onClick = { selectedProviderId = provider.id }
                        )
                        Text(
                            text = provider.name,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )
                    }
                }
            }

            Divider()

            OutlinedTextField(
                value = policyNumber,
                onValueChange = { policyNumber = it },
                label = { Text("Policy Number *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = insuranceType,
                onValueChange = { insuranceType = it },
                label = { Text("Insurance Type") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Medical, Dental, Medicare") }
            )

            OutlinedTextField(
                value = coverageType,
                onValueChange = { coverageType = it },
                label = { Text("Coverage Type") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., PPO, HMO") }
            )

            OutlinedTextField(
                value = memberPhone,
                onValueChange = { memberPhone = it },
                label = { Text("Member Phone") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = providerPhone,
                onValueChange = { providerPhone = it },
                label = { Text("Provider Phone") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    selectedProfileId?.let { profileId ->
                        selectedProviderId?.let { providerId ->
                            if (policyNumber.isNotBlank()) {
                                if (existingPolicy != null) {
                                    viewModel.updateInsurancePolicy(
                                        existingPolicy.copy(
                                            providerId = providerId,
                                            policyNumber = policyNumber.trim(),
                                            memberPhone = memberPhone.trim().takeIf { it.isNotEmpty() },
                                            providerPhone = providerPhone.trim().takeIf { it.isNotEmpty() },
                                            coverageType = coverageType.trim().takeIf { it.isNotEmpty() },
                                            insuranceType = insuranceType.trim().takeIf { it.isNotEmpty() },
                                            notes = notes.trim().takeIf { it.isNotEmpty() }
                                        )
                                    )
                                } else {
                                    viewModel.addInsurancePolicy(
                                        InsurancePolicy(
                                            profileId = profileId,
                                            providerId = providerId,
                                            policyNumber = policyNumber.trim(),
                                            memberPhone = memberPhone.trim().takeIf { it.isNotEmpty() },
                                            providerPhone = providerPhone.trim().takeIf { it.isNotEmpty() },
                                            coverageType = coverageType.trim().takeIf { it.isNotEmpty() },
                                            insuranceType = insuranceType.trim().takeIf { it.isNotEmpty() },
                                            notes = notes.trim().takeIf { it.isNotEmpty() }
                                        )
                                    )
                                }
                                onNavigateBack()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = policyNumber.isNotBlank() && selectedProviderId != null
            ) {
                Text("Save")
            }
        }
    }

    if (showProviderDialog) {
        AlertDialog(
            onDismissRequest = { showProviderDialog = false },
            title = { Text("Add Insurance Provider") },
            text = {
                OutlinedTextField(
                    value = newProviderName,
                    onValueChange = { newProviderName = it },
                    label = { Text("Provider Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newProviderName.isNotBlank()) {
                            viewModel.addInsuranceProvider(
                                InsuranceProvider(name = newProviderName.trim())
                            )
                            newProviderName = ""
                            showProviderDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProviderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
