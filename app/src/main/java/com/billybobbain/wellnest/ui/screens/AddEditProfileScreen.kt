package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.Profile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProfileScreen(
    viewModel: WellnestViewModel,
    profileId: Long?,
    onNavigateBack: () -> Unit
) {
    val profiles by viewModel.allProfiles.collectAsState()
    val existingProfile = profiles.find { it.id == profileId }

    var name by remember { mutableStateOf(existingProfile?.name ?: "") }
    var notes by remember { mutableStateOf(existingProfile?.notes ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (profileId == null) "Add Profile" else "Edit Profile") },
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
                value = name,
                onValueChange = { name = it },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        if (existingProfile != null) {
                            viewModel.updateProfile(
                                existingProfile.copy(
                                    name = name.trim(),
                                    notes = notes.trim().takeIf { it.isNotEmpty() }
                                )
                            )
                        } else {
                            viewModel.addProfile(
                                Profile(
                                    name = name.trim(),
                                    notes = notes.trim().takeIf { it.isNotEmpty() }
                                )
                            )
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}
