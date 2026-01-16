package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.Supply
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliesScreen(
    viewModel: WellnestViewModel,
    onNavigateBack: () -> Unit,
    onAddSupply: () -> Unit,
    onEditSupply: (Long) -> Unit
) {
    val supplies by viewModel.supplies.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Supplies") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSupply) {
                Icon(Icons.Default.Add, contentDescription = "Add Supply")
            }
        }
    ) { padding ->
        if (supplies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No supplies tracked yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(supplies) { supply ->
                    SupplyCard(
                        supply = supply,
                        onClick = { onEditSupply(supply.id) },
                        onMarkReplenished = {
                            viewModel.updateSupply(
                                supply.copy(lastReplenished = System.currentTimeMillis())
                            )
                        },
                        onDelete = { viewModel.deleteSupply(supply) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplyCard(
    supply: Supply,
    onClick: () -> Unit,
    onMarkReplenished: () -> Unit,
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
                    text = supply.itemName,
                    style = MaterialTheme.typography.titleMedium
                )

                // Show last replenished date
                if (supply.lastReplenished != null) {
                    val daysAgo = ((System.currentTimeMillis() - supply.lastReplenished) / (1000 * 60 * 60 * 24)).toInt()
                    val timeText = when {
                        daysAgo == 0 -> "Today"
                        daysAgo == 1 -> "Yesterday"
                        daysAgo < 7 -> "$daysAgo days ago"
                        daysAgo < 14 -> "1 week ago"
                        daysAgo < 30 -> "${daysAgo / 7} weeks ago"
                        else -> "${daysAgo / 30} months ago"
                    }
                    Text(
                        text = "Last: $timeText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Never replenished",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (!supply.notes.isNullOrEmpty()) {
                    Text(
                        text = supply.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row {
                IconButton(onClick = onMarkReplenished) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Mark as Replenished",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
