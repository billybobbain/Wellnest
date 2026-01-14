package com.billybobbain.wellnest.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.utils.ClaudeApiService
import com.billybobbain.wellnest.utils.EncryptedPrefsManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WellnestViewModel,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val selectedTheme = settings?.selectedTheme ?: "Teal"

    val themes = listOf("Teal", "Purple", "Blue", "Green", "Orange", "Pink")

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // AI Clarification state
    var aiEnabled by remember { mutableStateOf(EncryptedPrefsManager.isAiEnabled(context)) }
    var apiKey by remember { mutableStateOf(EncryptedPrefsManager.getClaudeApiKey(context) ?: "") }
    var showApiKey by remember { mutableStateOf(false) }
    var testingConnection by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Theme",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            themes.forEach { theme ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.updateTheme(theme) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(theme)
                        RadioButton(
                            selected = selectedTheme == theme,
                            onClick = { viewModel.updateTheme(theme) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            // AI Clarification Section
            Text(
                "AI Message Clarification (Optional)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Enable AI Clarification",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = aiEnabled,
                    onCheckedChange = { enabled ->
                        aiEnabled = enabled
                        EncryptedPrefsManager.setAiEnabled(context, enabled)
                    }
                )
            }

            if (aiEnabled) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = {
                        apiKey = it
                        EncryptedPrefsManager.setClaudeApiKey(context, it.trim().takeIf { key -> key.isNotEmpty() })
                        testResult = null
                    },
                    label = { Text("Claude API Key") },
                    placeholder = { Text("sk-ant-api03-...") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        TextButton(onClick = { showApiKey = !showApiKey }) {
                            Text(
                                text = if (showApiKey) "Hide" else "Show",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (apiKey.isNotBlank()) {
                            testingConnection = true
                            testResult = null
                            scope.launch {
                                val result = ClaudeApiService.testApiKey(apiKey.trim())
                                testingConnection = false
                                testResult = result.fold(
                                    onSuccess = { it },
                                    onFailure = { "Error: ${it.message}" }
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = apiKey.isNotBlank() && !testingConnection
                ) {
                    if (testingConnection) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (testingConnection) "Testing..." else "Test Connection")
                }

                if (testResult != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = testResult!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (testResult!!.startsWith("Error")) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "ℹ️ How to get an API key:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "1. Visit console.anthropic.com\n2. Sign up (free tier includes \$5 credit)\n3. Generate an API key\n4. Paste it above",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Cost: ~\$0.0002 per message\nFree tier: ~25,000 messages",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "🔒 Your API key is stored encrypted and never sent anywhere except Claude API.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { viewModel.generateTestProfile() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate Test Profile (Bilbo)")
            }

            Text(
                "Creates a complete test profile with sample data for screenshots",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
