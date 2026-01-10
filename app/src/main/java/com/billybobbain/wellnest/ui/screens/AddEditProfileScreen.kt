package com.billybobbain.wellnest.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.billybobbain.wellnest.data.Profile
import com.billybobbain.wellnest.utils.ImageUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProfileScreen(
    viewModel: WellnestViewModel,
    profileId: Long?,
    onNavigateBack: () -> Unit
) {
    val profiles by viewModel.allProfiles.collectAsState()
    val existingProfile = profiles.find { it.id == profileId }
    val context = LocalContext.current

    var name by remember { mutableStateOf(existingProfile?.name ?: "") }
    var notes by remember { mutableStateOf(existingProfile?.notes ?: "") }
    var photoUri by remember { mutableStateOf(existingProfile?.photoUri) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Photo picker launcher
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

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
            // Profile Photo Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Display photo or placeholder
                val displayUri = selectedImageUri?.toString() ?: photoUri

                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    if (displayUri != null) {
                        AsyncImage(
                            model = File(displayUri),
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "No Photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Text(if (displayUri == null) "Add Photo" else "Change Photo")
                    }

                    if (displayUri != null) {
                        OutlinedButton(
                            onClick = {
                                selectedImageUri = null
                                photoUri = null
                            }
                        ) {
                            Text("Remove")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
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
                    if (name.isNotBlank()) {
                        // Get the profile ID we'll be saving to
                        val targetProfileId = existingProfile?.id ?: 0L

                        // Process new image if selected
                        val finalPhotoUri = if (selectedImageUri != null) {
                            // Save new image and get its path
                            ImageUtils.saveProfileImage(context, selectedImageUri!!, targetProfileId)
                        } else {
                            photoUri // Keep existing or null
                        }

                        if (existingProfile != null) {
                            // Delete old image if it changed
                            if (finalPhotoUri != existingProfile.photoUri && existingProfile.photoUri != null) {
                                ImageUtils.deleteProfileImage(context, existingProfile.photoUri)
                            }

                            viewModel.updateProfile(
                                existingProfile.copy(
                                    name = name.trim(),
                                    notes = notes.trim().takeIf { it.isNotEmpty() },
                                    photoUri = finalPhotoUri
                                )
                            )
                        } else {
                            viewModel.addProfile(
                                Profile(
                                    name = name.trim(),
                                    notes = notes.trim().takeIf { it.isNotEmpty() },
                                    photoUri = finalPhotoUri
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
