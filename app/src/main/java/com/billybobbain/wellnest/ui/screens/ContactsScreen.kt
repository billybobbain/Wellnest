package com.billybobbain.wellnest.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.Contact
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: WellnestViewModel,
    onNavigateBack: () -> Unit,
    onAddContact: () -> Unit,
    onEditContact: (Long) -> Unit
) {
    val contacts by viewModel.contacts.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Contacts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val shareText = formatContactsForSharing(contacts, currentProfile?.name ?: "Patient")
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                putExtra(Intent.EXTRA_SUBJECT, "Medical Contact List")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Contacts"))
                        },
                        enabled = contacts.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Contacts")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddContact) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { padding ->
        if (contacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No contacts yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contacts) { contact ->
                    ContactCard(
                        contact = contact,
                        onClick = { onEditContact(contact.id) },
                        onDelete = { viewModel.deleteContact(contact) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactCard(
    contact: Contact,
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
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (!contact.role.isNullOrEmpty()) {
                    Text(
                        text = contact.role,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!contact.phone.isNullOrEmpty()) {
                    Text(
                        text = contact.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (!contact.email.isNullOrEmpty()) {
                    Text(
                        text = contact.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

/**
 * Format contacts list for sharing
 */
private fun formatContactsForSharing(contacts: List<Contact>, profileName: String): String {
    if (contacts.isEmpty()) {
        return "No contacts to share"
    }

    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    return buildString {
        appendLine("MEDICAL CONTACT LIST")
        appendLine("Patient: $profileName")
        appendLine("Generated: ${dateFormat.format(Date())}")
        appendLine()
        appendLine("=".repeat(40))
        appendLine()

        contacts.forEachIndexed { index, contact ->
            appendLine("${index + 1}. ${contact.name}")

            if (!contact.role.isNullOrEmpty()) {
                appendLine("   Role: ${contact.role}")
            }

            if (!contact.phone.isNullOrEmpty()) {
                appendLine("   Phone: ${contact.phone}")
            }

            if (!contact.email.isNullOrEmpty()) {
                appendLine("   Email: ${contact.email}")
            }

            if (!contact.notes.isNullOrEmpty()) {
                appendLine("   Notes: ${contact.notes}")
            }

            appendLine()
        }

        appendLine("=".repeat(40))
        appendLine("Total: ${contacts.size} contact(s)")
    }
}
