package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.Contact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditContactScreen(
    viewModel: WellnestViewModel,
    contactId: Long?,
    onNavigateBack: () -> Unit
) {
    val contacts by viewModel.contacts.collectAsState()
    val selectedProfileId by viewModel.selectedProfileId.collectAsState()
    val existingContact = contacts.find { it.id == contactId }

    var name by remember { mutableStateOf(existingContact?.name ?: "") }
    var role by remember { mutableStateOf(existingContact?.role ?: "") }
    var phone by remember { mutableStateOf(existingContact?.phone ?: "") }
    var email by remember { mutableStateOf(existingContact?.email ?: "") }
    var notes by remember { mutableStateOf(existingContact?.notes ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (contactId == null) "Add Contact" else "Edit Contact") },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            OutlinedTextField(
                value = role,
                onValueChange = { role = it },
                label = { Text("Role/Title") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Nurse, Care Manager") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    selectedProfileId?.let { profileId ->
                        if (name.isNotBlank()) {
                            if (existingContact != null) {
                                viewModel.updateContact(
                                    existingContact.copy(
                                        name = name.trim(),
                                        role = role.trim().takeIf { it.isNotEmpty() },
                                        phone = phone.trim().takeIf { it.isNotEmpty() },
                                        email = email.trim().takeIf { it.isNotEmpty() },
                                        notes = notes.trim().takeIf { it.isNotEmpty() }
                                    )
                                )
                            } else {
                                viewModel.addContact(
                                    Contact(
                                        profileId = profileId,
                                        name = name.trim(),
                                        role = role.trim().takeIf { it.isNotEmpty() },
                                        phone = phone.trim().takeIf { it.isNotEmpty() },
                                        email = email.trim().takeIf { it.isNotEmpty() },
                                        notes = notes.trim().takeIf { it.isNotEmpty() }
                                    )
                                )
                            }
                            onNavigateBack()
                        }
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
