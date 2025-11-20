package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: WellnestViewModel,
    onNavigateToMedications: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToHealthProfile: () -> Unit,
    onNavigateToInsurance: () -> Unit,
    onNavigateToSecurityCodes: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSwitchProfile: () -> Unit
) {
    val currentProfile by viewModel.currentProfile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentProfile?.name ?: "Wellnest") },
                actions = {
                    IconButton(onClick = onSwitchProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Switch Profile")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MenuCard(
                    title = "Medications",
                    icon = Icons.Default.Add,
                    onClick = onNavigateToMedications
                )
            }
            item {
                MenuCard(
                    title = "Appointments",
                    icon = Icons.Default.DateRange,
                    onClick = onNavigateToAppointments
                )
            }
            item {
                MenuCard(
                    title = "Contacts",
                    icon = Icons.Default.Phone,
                    onClick = onNavigateToContacts
                )
            }
            item {
                MenuCard(
                    title = "Health Profile",
                    icon = Icons.Default.Face,
                    onClick = onNavigateToHealthProfile
                )
            }
            item {
                MenuCard(
                    title = "Insurance",
                    icon = Icons.Default.AccountBox,
                    onClick = onNavigateToInsurance
                )
            }
            item {
                MenuCard(
                    title = "Security Codes",
                    icon = Icons.Default.Lock,
                    onClick = onNavigateToSecurityCodes
                )
            }
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}
