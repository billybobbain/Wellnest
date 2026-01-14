package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.Message
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    viewModel: WellnestViewModel,
    onNavigateBack: () -> Unit,
    onAddMessage: () -> Unit,
    onEditMessage: (Long) -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMessage) {
                Icon(Icons.Default.Add, contentDescription = "Add Message")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Profile context bar
            currentProfile?.let { profile ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Profile picture
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            if (profile.photoUri != null) {
                                AsyncImage(
                                    model = File(profile.photoUri),
                                    contentDescription = "Profile picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Profile picture",
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Messages for ${profile.name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No messages yet")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { message ->
                        MessageCard(
                            message = message,
                            onClick = { onEditMessage(message.id) },
                            onDelete = { viewModel.deleteMessage(message) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageCard(
    message: Message,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.US)

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
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateFormat.format(Date(message.timestamp)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Show clarified version by default if available, otherwise show original
                val displayText = message.interpretedText ?: message.originalText
                Text(
                    text = displayText.take(100) + if (displayText.length > 100) "..." else "",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (message.interpretedText != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✓ Clarified",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Not clarified yet",
                        style = MaterialTheme.typography.labelSmall,
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
