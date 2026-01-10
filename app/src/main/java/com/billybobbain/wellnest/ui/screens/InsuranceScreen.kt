package com.billybobbain.wellnest.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.InsurancePolicy
import com.billybobbain.wellnest.data.InsuranceProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsuranceScreen(
    viewModel: WellnestViewModel,
    onNavigateBack: () -> Unit,
    onAddPolicy: () -> Unit,
    onEditPolicy: (Long) -> Unit
) {
    val policies by viewModel.insurancePolicies.collectAsState()
    val providers by viewModel.insuranceProviders.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insurance") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val shareText = formatInsuranceForSharing(policies, providers, currentProfile?.name ?: "Patient")

                            // Collect all card photo URIs using FileProvider
                            val imageUris = mutableListOf<Uri>()

                            policies.forEach { policy ->
                                policy.frontCardPhotoUri?.let { filePath ->
                                    try {
                                        val file = File(filePath)
                                        if (file.exists()) {
                                            val contentUri = FileProvider.getUriForFile(
                                                context,
                                                "com.billybobbain.wellnest.fileprovider",
                                                file
                                            )
                                            imageUris.add(contentUri)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                policy.backCardPhotoUri?.let { filePath ->
                                    try {
                                        val file = File(filePath)
                                        if (file.exists()) {
                                            val contentUri = FileProvider.getUriForFile(
                                                context,
                                                "com.billybobbain.wellnest.fileprovider",
                                                file
                                            )
                                            imageUris.add(contentUri)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }

                            val intent = if (imageUris.isNotEmpty()) {
                                // Share images with text in message body
                                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                    type = "image/*"
                                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(imageUris))
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    putExtra(Intent.EXTRA_SUBJECT, "Insurance Information")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                            } else {
                                // Share text only
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    putExtra(Intent.EXTRA_SUBJECT, "Insurance Information")
                                }
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Insurance"))
                        },
                        enabled = policies.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Insurance")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPolicy) {
                Icon(Icons.Default.Add, contentDescription = "Add Policy")
            }
        }
    ) { padding ->
        if (policies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No insurance policies yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(policies) { policy ->
                    val provider = providers.find { it.id == policy.providerId }
                    InsurancePolicyCard(
                        policy = policy,
                        providerName = provider?.name ?: "Unknown Provider",
                        onClick = { onEditPolicy(policy.id) },
                        onDelete = { viewModel.deleteInsurancePolicy(policy) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsurancePolicyCard(
    policy: InsurancePolicy,
    providerName: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = providerName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Policy: ${policy.policyNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!policy.insuranceType.isNullOrEmpty() || !policy.coverageType.isNullOrEmpty()) {
                        Text(
                            text = listOfNotNull(policy.insuranceType, policy.coverageType)
                                .joinToString(" - "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!policy.memberPhone.isNullOrEmpty()) {
                        Text(
                            text = "Member: ${policy.memberPhone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }

            // Card photos
            if (policy.frontCardPhotoUri != null || policy.backCardPhotoUri != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Front card thumbnail
                    if (policy.frontCardPhotoUri != null) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Front",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = Uri.parse(policy.frontCardPhotoUri),
                                    contentDescription = "Front of insurance card",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    // Back card thumbnail
                    if (policy.backCardPhotoUri != null) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Back",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = Uri.parse(policy.backCardPhotoUri),
                                    contentDescription = "Back of insurance card",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    // If only one card photo, add spacer for symmetry
                    if (policy.frontCardPhotoUri == null || policy.backCardPhotoUri == null) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Format insurance policies for sharing
 */
private fun formatInsuranceForSharing(
    policies: List<InsurancePolicy>,
    providers: List<InsuranceProvider>,
    profileName: String
): String {
    if (policies.isEmpty()) {
        return "No insurance information to share"
    }

    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    return buildString {
        appendLine("INSURANCE INFORMATION")
        appendLine("Patient: $profileName")
        appendLine("Generated: ${dateFormat.format(Date())}")
        appendLine()
        appendLine("=".repeat(40))
        appendLine()

        policies.forEachIndexed { index, policy ->
            val provider = providers.find { it.id == policy.providerId }

            appendLine("${index + 1}. ${provider?.name ?: "Unknown Provider"}")
            appendLine("   Policy Number: ${policy.policyNumber}")

            if (!policy.insuranceType.isNullOrEmpty()) {
                appendLine("   Type: ${policy.insuranceType}")
            }

            if (!policy.coverageType.isNullOrEmpty()) {
                appendLine("   Coverage: ${policy.coverageType}")
            }

            if (!policy.memberPhone.isNullOrEmpty()) {
                appendLine("   Member Services: ${policy.memberPhone}")
            }

            appendLine()
        }

        appendLine("=".repeat(40))
        appendLine("Total: ${policies.size} policy/policies")
    }
}
