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
import com.billybobbain.wellnest.data.InsurancePolicy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsuranceScreen(
    viewModel: WellnestViewModel,
    onNavigateBack: () -> Unit,
    onAddPolicy: () -> Unit,
    onEditPolicy: (Long) -> Unit
) {
    val policies by viewModel.insurancePolicies.collectAsState()
    val providers by viewModel.insuranceProviders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insurance") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPolicy) {
                Icon(Icons.Default.Add, contentDescription = "Add Policy")
            }
        }
    ) { padding ->
        if (policies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No insurance policies yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(policies) { policy ->
                    val provider = providers.find { it.id == policy.providerId }
                    InsurancePolicyCard(
                        policy = policy,
                        providerName = provider?.name ?: "Unknown Provider",
                        onClick = { onEditPolicy(policy.id) },
                        onDelete = { viewModel.deleteInsurancePolicy(policy) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsurancePolicyCard(
    policy: InsurancePolicy,
    providerName: String,
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
                    text = providerName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Policy: ${policy.policyNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!policy.insuranceType.isNullOrEmpty() || !policy.coverageType.isNullOrEmpty()) {
                    Text(
                        text = listOfNotNull(policy.insuranceType, policy.coverageType)
                            .joinToString(" - "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!policy.memberPhone.isNullOrEmpty()) {
                    Text(
                        text = "Member: ${policy.memberPhone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
