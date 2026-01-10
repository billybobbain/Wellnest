package com.billybobbain.wellnest.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.InsurancePolicy
import com.billybobbain.wellnest.data.InsuranceProvider
import com.billybobbain.wellnest.utils.ImageUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditInsurancePolicyScreen(
    viewModel: WellnestViewModel,
    policyId: Long?,
    onNavigateBack: () -> Unit
) {
    val policies by viewModel.insurancePolicies.collectAsState()
    val providers by viewModel.insuranceProviders.collectAsState()
    val selectedProfileId by viewModel.selectedProfileId.collectAsState()
    val existingPolicy = policies.find { it.id == policyId }

    var selectedProviderId by remember { mutableStateOf(existingPolicy?.providerId) }
    var policyNumber by remember { mutableStateOf(existingPolicy?.policyNumber ?: "") }
    var memberPhone by remember { mutableStateOf(existingPolicy?.memberPhone ?: "") }
    var providerPhone by remember { mutableStateOf(existingPolicy?.providerPhone ?: "") }
    var coverageType by remember { mutableStateOf(existingPolicy?.coverageType ?: "") }
    var insuranceType by remember { mutableStateOf(existingPolicy?.insuranceType ?: "") }
    var notes by remember { mutableStateOf(existingPolicy?.notes ?: "") }
    var showProviderDialog by remember { mutableStateOf(false) }
    var newProviderName by remember { mutableStateOf("") }

    // Card photo state
    var frontCardPhotoUri by remember { mutableStateOf(existingPolicy?.frontCardPhotoUri) }
    var backCardPhotoUri by remember { mutableStateOf(existingPolicy?.backCardPhotoUri) }
    var selectedFrontCardUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBackCardUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    // Front card photo picker
    val frontCardPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { selectedFrontCardUri = it }
    }

    // Back card photo picker
    val backCardPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { selectedBackCardUri = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (policyId == null) "Add Insurance" else "Edit Insurance") },
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
            // Provider selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Insurance Provider *", style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = { showProviderDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Provider")
                }
            }

            if (providers.isEmpty()) {
                Text("No providers yet. Add one using the + button above.")
            } else {
                providers.forEach { provider ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RadioButton(
                            selected = selectedProviderId == provider.id,
                            onClick = { selectedProviderId = provider.id }
                        )
                        Text(
                            text = provider.name,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )
                    }
                }
            }

            Divider()

            // Card Photos Section
            Text("Insurance Card Photos", style = MaterialTheme.typography.titleSmall)

            // Front Card Photo
            Text("Front of Card", style = MaterialTheme.typography.bodyMedium)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                val displayUri = selectedFrontCardUri
                    ?: frontCardPhotoUri?.let { Uri.parse(it) }

                if (displayUri != null) {
                    AsyncImage(
                        model = displayUri,
                        contentDescription = "Front of insurance card",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.AccountBox,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No front card image",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        frontCardPhotoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (selectedFrontCardUri != null || frontCardPhotoUri != null) "Change" else "Add Photo")
                }
                if (selectedFrontCardUri != null || frontCardPhotoUri != null) {
                    OutlinedButton(
                        onClick = {
                            selectedFrontCardUri = null
                            frontCardPhotoUri = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Remove")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Back Card Photo
            Text("Back of Card", style = MaterialTheme.typography.bodyMedium)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                val displayUri = selectedBackCardUri
                    ?: backCardPhotoUri?.let { Uri.parse(it) }

                if (displayUri != null) {
                    AsyncImage(
                        model = displayUri,
                        contentDescription = "Back of insurance card",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.AccountBox,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No back card image",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        backCardPhotoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (selectedBackCardUri != null || backCardPhotoUri != null) "Change" else "Add Photo")
                }
                if (selectedBackCardUri != null || backCardPhotoUri != null) {
                    OutlinedButton(
                        onClick = {
                            selectedBackCardUri = null
                            backCardPhotoUri = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Remove")
                    }
                }
            }

            Divider()

            OutlinedTextField(
                value = policyNumber,
                onValueChange = { policyNumber = it },
                label = { Text("Policy Number *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = insuranceType,
                onValueChange = { insuranceType = it },
                label = { Text("Insurance Type") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Medical, Dental, Medicare") }
            )

            OutlinedTextField(
                value = coverageType,
                onValueChange = { coverageType = it },
                label = { Text("Coverage Type") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., PPO, HMO") }
            )

            OutlinedTextField(
                value = memberPhone,
                onValueChange = { memberPhone = it },
                label = { Text("Member Phone") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = providerPhone,
                onValueChange = { providerPhone = it },
                label = { Text("Provider Phone") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    selectedProfileId?.let { profileId ->
                        selectedProviderId?.let { providerId ->
                            if (policyNumber.isNotBlank()) {
                                // Use existing policy ID or 0 for new policies
                                val cardPolicyId = existingPolicy?.id ?: 0L

                                // Process front card photo
                                val finalFrontCardUri = if (selectedFrontCardUri != null) {
                                    // New photo selected - save it and delete old one if exists
                                    frontCardPhotoUri?.let { oldUri ->
                                        ImageUtils.deleteInsuranceCardImage(context, oldUri)
                                    }
                                    ImageUtils.saveInsuranceCardImage(context, selectedFrontCardUri!!, cardPolicyId, "front")
                                } else if (frontCardPhotoUri == null) {
                                    // Photo was removed - delete old photo if exists
                                    existingPolicy?.frontCardPhotoUri?.let { oldUri ->
                                        ImageUtils.deleteInsuranceCardImage(context, oldUri)
                                    }
                                    null
                                } else {
                                    // Keep existing photo
                                    frontCardPhotoUri
                                }

                                // Process back card photo
                                val finalBackCardUri = if (selectedBackCardUri != null) {
                                    // New photo selected - save it and delete old one if exists
                                    backCardPhotoUri?.let { oldUri ->
                                        ImageUtils.deleteInsuranceCardImage(context, oldUri)
                                    }
                                    ImageUtils.saveInsuranceCardImage(context, selectedBackCardUri!!, cardPolicyId, "back")
                                } else if (backCardPhotoUri == null) {
                                    // Photo was removed - delete old photo if exists
                                    existingPolicy?.backCardPhotoUri?.let { oldUri ->
                                        ImageUtils.deleteInsuranceCardImage(context, oldUri)
                                    }
                                    null
                                } else {
                                    // Keep existing photo
                                    backCardPhotoUri
                                }

                                if (existingPolicy != null) {
                                    viewModel.updateInsurancePolicy(
                                        existingPolicy.copy(
                                            providerId = providerId,
                                            policyNumber = policyNumber.trim(),
                                            memberPhone = memberPhone.trim().takeIf { it.isNotEmpty() },
                                            providerPhone = providerPhone.trim().takeIf { it.isNotEmpty() },
                                            coverageType = coverageType.trim().takeIf { it.isNotEmpty() },
                                            insuranceType = insuranceType.trim().takeIf { it.isNotEmpty() },
                                            notes = notes.trim().takeIf { it.isNotEmpty() },
                                            frontCardPhotoUri = finalFrontCardUri,
                                            backCardPhotoUri = finalBackCardUri
                                        )
                                    )
                                } else {
                                    viewModel.addInsurancePolicy(
                                        InsurancePolicy(
                                            profileId = profileId,
                                            providerId = providerId,
                                            policyNumber = policyNumber.trim(),
                                            memberPhone = memberPhone.trim().takeIf { it.isNotEmpty() },
                                            providerPhone = providerPhone.trim().takeIf { it.isNotEmpty() },
                                            coverageType = coverageType.trim().takeIf { it.isNotEmpty() },
                                            insuranceType = insuranceType.trim().takeIf { it.isNotEmpty() },
                                            notes = notes.trim().takeIf { it.isNotEmpty() },
                                            frontCardPhotoUri = finalFrontCardUri,
                                            backCardPhotoUri = finalBackCardUri
                                        )
                                    )
                                }
                                onNavigateBack()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = policyNumber.isNotBlank() && selectedProviderId != null
            ) {
                Text("Save")
            }
        }
    }

    if (showProviderDialog) {
        AlertDialog(
            onDismissRequest = { showProviderDialog = false },
            title = { Text("Add Insurance Provider") },
            text = {
                OutlinedTextField(
                    value = newProviderName,
                    onValueChange = { newProviderName = it },
                    label = { Text("Provider Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newProviderName.isNotBlank()) {
                            viewModel.addInsuranceProvider(
                                InsuranceProvider(name = newProviderName.trim())
                            )
                            newProviderName = ""
                            showProviderDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProviderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
