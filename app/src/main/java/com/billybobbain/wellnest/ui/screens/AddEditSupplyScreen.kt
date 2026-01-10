package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.Supply

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSupplyScreen(
    viewModel: WellnestViewModel,
    supplyId: Long?,
    onNavigateBack: () -> Unit
) {
    val supplies by viewModel.supplies.collectAsState()
    val selectedProfileId by viewModel.selectedProfileId.collectAsState()
    val existingSupply = supplies.find { it.id == supplyId }

    var itemName by remember { mutableStateOf(existingSupply?.itemName ?: "") }
    var notes by remember { mutableStateOf(existingSupply?.notes ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (supplyId == null) "Add Supply" else "Edit Supply") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                label = { Text("Item Name *") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Milk, Dr. B") }
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                placeholder = { Text("Any special notes...") }
            )

            Text(
                text = "Tip: Use the refresh button on the list to mark items as replenished",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    selectedProfileId?.let { profileId ->
                        if (itemName.isNotBlank()) {
                            if (existingSupply != null) {
                                viewModel.updateSupply(
                                    existingSupply.copy(
                                        itemName = itemName.trim(),
                                        notes = notes.trim().takeIf { it.isNotEmpty() }
                                    )
                                )
                            } else {
                                viewModel.addSupply(
                                    Supply(
                                        profileId = profileId,
                                        itemName = itemName.trim(),
                                        notes = notes.trim().takeIf { it.isNotEmpty() }
                                    )
                                )
                            }
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = itemName.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}
