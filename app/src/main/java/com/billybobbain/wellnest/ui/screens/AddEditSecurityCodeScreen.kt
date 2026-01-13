package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.SecurityCode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSecurityCodeScreen(
    viewModel: WellnestViewModel,
    codeId: Long?,
    onNavigateBack: () -> Unit
) {
    val securityCodes by viewModel.securityCodes.collectAsState()
    val selectedProfileId by viewModel.selectedProfileId.collectAsState()
    val existingCode = securityCodes.find { it.id == codeId }

    var label by remember { mutableStateOf(existingCode?.label ?: "") }
    var code by remember { mutableStateOf(existingCode?.code ?: "") }
    var notes by remember { mutableStateOf(existingCode?.notes ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (codeId == null) "Add Security Code" else "Edit Security Code") },
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
                value = label,
                onValueChange = { label = it },
                label = { Text("Label *") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Front Door, Room Access") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Code *") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., 1234") }
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    selectedProfileId?.let { profileId ->
                        if (label.isNotBlank() && code.isNotBlank()) {
                            if (existingCode != null) {
                                viewModel.updateSecurityCode(
                                    existingCode.copy(
                                        label = label.trim(),
                                        code = code.trim(),
                                        notes = notes.trim().takeIf { it.isNotEmpty() }
                                    )
                                )
                            } else {
                                viewModel.addSecurityCode(
                                    SecurityCode(
                                        profileId = profileId,
                                        label = label.trim(),
                                        code = code.trim(),
                                        notes = notes.trim().takeIf { it.isNotEmpty() }
                                    )
                                )
                            }
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = label.isNotBlank() && code.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}
