package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.Profile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSelectionScreen(
    viewModel: WellnestViewModel,
    onProfileSelected: (Long) -> Unit,
    onAddProfile: () -> Unit
) {
    val profiles by viewModel.allProfiles.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Profile") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProfile) {
                Icon(Icons.Default.Add, contentDescription = "Add Profile")
            }
        }
    ) { padding ->
        if (profiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No profiles yet")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onAddProfile) {
                        Text("Add Profile")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(profiles) { profile ->
                    ProfileCard(
                        profile = profile,
                        onClick = { onProfileSelected(profile.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileCard(
    profile: Profile,
    onClick: () -> Unit
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (!profile.notes.isNullOrEmpty()) {
                    Text(
                        text = profile.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
