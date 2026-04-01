package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
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
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SuppliesScreen(
    viewModel: WellnestViewModel,
    onNavigateBack: () -> Unit,
    onAddSupply: () -> Unit,
    onEditSupply: (Long) -> Unit
) {
    val supplies by viewModel.supplies.collectAsState()
    val lazyListState = rememberLazyListState()

    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val reordered = supplies.toMutableList()
        val item = reordered.removeAt(from.index)
        reordered.add(to.index, item)
        viewModel.reorderSupplies(reordered)
    }

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
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(supplies, key = { it.id }) { supply ->
                    ReorderableItem(reorderableState, key = supply.id) { isDragging ->
                        SupplyCard(
                            supply = supply,
                            isDragging = isDragging,
                            onClick = { onEditSupply(supply.id) },
                            onMarkReplenished = {
                                viewModel.updateSupply(
                                    supply.copy(lastReplenished = System.currentTimeMillis())
                                )
                            },
                            onDelete = { viewModel.deleteSupply(supply) },
                            dragModifier = Modifier.longPressDraggableHandle()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplyCard(
    supply: Supply,
    isDragging: Boolean = false,
    onClick: () -> Unit,
    onMarkReplenished: () -> Unit,
    onDelete: () -> Unit,
    dragModifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) MaterialTheme.colorScheme.tertiaryContainer
                            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 8.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Drag to reorder",
                modifier = dragModifier.padding(end = 12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = supply.itemName,
                    style = MaterialTheme.typography.titleMedium
                )

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
