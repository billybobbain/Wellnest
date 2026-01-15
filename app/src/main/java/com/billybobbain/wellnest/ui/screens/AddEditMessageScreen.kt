package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.Message
import com.billybobbain.wellnest.utils.ClaudeApiService
import com.billybobbain.wellnest.utils.EncryptedPrefsManager
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMessageScreen(
    viewModel: WellnestViewModel,
    messageId: Long?,
    onNavigateBack: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val selectedProfileId by viewModel.selectedProfileId.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    val existingMessage = messages.find { it.id == messageId }

    var originalText by remember { mutableStateOf(existingMessage?.originalText ?: "") }
    var interpretedText by remember { mutableStateOf(existingMessage?.interpretedText ?: "") }
    var notes by remember { mutableStateOf(existingMessage?.notes ?: "") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // AI Clarification state
    val aiEnabled = remember { EncryptedPrefsManager.isAiEnabled(context) }
    val apiKey = remember { EncryptedPrefsManager.getClaudeApiKey(context) }
    var clarifying by remember { mutableStateOf(false) }
    var clarifyError by remember { mutableStateOf<String?>(null) }

    // Track previous text length to detect paste
    var previousTextLength by remember { mutableStateOf(originalText.length) }

    // Function to trigger clarification
    fun triggerClarification(text: String) {
        if (text.isNotBlank() && aiEnabled && !apiKey.isNullOrBlank() && !clarifying) {
            clarifying = true
            clarifyError = null
            scope.launch {
                val result = ClaudeApiService.clarifyMessage(text.trim(), apiKey)
                clarifying = false
                result.fold(
                    onSuccess = { clarified ->
                        interpretedText = clarified
                    },
                    onFailure = { error ->
                        clarifyError = error.message
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (messageId == null) "Add Message" else "View Message") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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
                            text = "Message for ${profile.name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Original Message",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = originalText,
                    onValueChange = { newText ->
                        val lengthDelta = newText.length - previousTextLength
                        originalText = newText
                        previousTextLength = newText.length

                        // Auto-clarify on paste (10+ characters added at once)
                        if (lengthDelta >= 10 && messageId == null) {
                            triggerClarification(newText)
                        }
                    },
                    label = { Text("Original Text *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    placeholder = { Text("Paste the original message here...") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                HorizontalDivider()

                Text(
                    text = "Clarified Version",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                // Clarify button (only if AI enabled and API key configured)
                if (aiEnabled && !apiKey.isNullOrBlank()) {
                    Button(
                        onClick = { triggerClarification(originalText) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = originalText.isNotBlank() && !clarifying
                    ) {
                        if (clarifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (clarifying) "Clarifying..." else "🔄 Clarify with AI")
                    }

                    if (clarifyError != null) {
                        Text(
                            text = clarifyError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "💡 AI Clarification: ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                if (!aiEnabled) {
                                    "Enable AI clarification in Settings to use this feature."
                                } else {
                                    "Configure your Claude API key in Settings to use AI clarification."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = interpretedText,
                    onValueChange = { interpretedText = it },
                    label = { Text("Clarified Text") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    placeholder = { Text("Paste the clarified version here...") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                HorizontalDivider()

                Text(
                    text = "Additional Notes",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    placeholder = { Text("Add context or reminders...") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        selectedProfileId?.let { profileId ->
                            if (originalText.isNotBlank()) {
                                if (existingMessage != null) {
                                    viewModel.updateMessage(
                                        existingMessage.copy(
                                            originalText = originalText.trim(),
                                            interpretedText = interpretedText.trim().takeIf { it.isNotEmpty() },
                                            notes = notes.trim().takeIf { it.isNotEmpty() }
                                        )
                                    )
                                } else {
                                    viewModel.addMessage(
                                        Message(
                                            profileId = profileId,
                                            originalText = originalText.trim(),
                                            interpretedText = interpretedText.trim().takeIf { it.isNotEmpty() },
                                            notes = notes.trim().takeIf { it.isNotEmpty() }
                                        )
                                    )
                                }
                                onNavigateBack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = originalText.isNotBlank()
                ) {
                    Text("Save")
                }
            }
        }
    }
}
