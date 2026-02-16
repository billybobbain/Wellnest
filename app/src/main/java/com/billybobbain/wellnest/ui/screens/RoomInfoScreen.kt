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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.utils.LocationUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomInfoScreen(
    viewModel: WellnestViewModel,
    onNavigateBack: () -> Unit
) {
    val currentProfile by viewModel.currentProfile.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var roomLength by remember { mutableStateOf("") }
    var roomWidth by remember { mutableStateOf("") }
    var roomHeight by remember { mutableStateOf("") }
    var windowWidth by remember { mutableStateOf("") }
    var windowHeight by remember { mutableStateOf("") }
    var roomNotes by remember { mutableStateOf("") }
    var homeAddress by remember { mutableStateOf("") }
    var homeLatitude by remember { mutableStateOf<Double?>(null) }
    var homeLongitude by remember { mutableStateOf<Double?>(null) }
    var geocoding by remember { mutableStateOf(false) }

    // Load current profile's room data
    LaunchedEffect(currentProfile) {
        currentProfile?.let { profile ->
            roomLength = profile.roomLength ?: ""
            roomWidth = profile.roomWidth ?: ""
            roomHeight = profile.roomHeight ?: ""
            windowWidth = profile.windowWidth ?: ""
            windowHeight = profile.windowHeight ?: ""
            roomNotes = profile.roomNotes ?: ""
            homeAddress = profile.homeAddress ?: ""
            homeLatitude = profile.homeLatitude
            homeLongitude = profile.homeLongitude
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Room Information") },
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
            Text(
                text = "Room Dimensions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = roomLength,
                    onValueChange = { roomLength = it },
                    label = { Text("Length") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("e.g., 12'") }
                )

                OutlinedTextField(
                    value = roomWidth,
                    onValueChange = { roomWidth = it },
                    label = { Text("Width") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("e.g., 10'") }
                )
            }

            OutlinedTextField(
                value = roomHeight,
                onValueChange = { roomHeight = it },
                label = { Text("Ceiling Height") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., 8'") }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Window Dimensions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = windowWidth,
                    onValueChange = { windowWidth = it },
                    label = { Text("Window Width") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("e.g., 48\"") }
                )

                OutlinedTextField(
                    value = windowHeight,
                    onValueChange = { windowHeight = it },
                    label = { Text("Window Height") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("e.g., 60\"") }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Additional Notes",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = roomNotes,
                onValueChange = { roomNotes = it },
                label = { Text("Room Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                placeholder = { Text("Additional room details...") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Home Address (for distance calculations)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = homeAddress,
                onValueChange = { homeAddress = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("123 Main St, Plano, TX 75024") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                trailingIcon = {
                    if (geocoding) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            )

            if (homeLatitude != null && homeLongitude != null) {
                Text(
                    text = "Coordinates: ${String.format("%.4f", homeLatitude)}, ${String.format("%.4f", homeLongitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    if (homeAddress.isNotBlank()) {
                        geocoding = true
                        scope.launch {
                            val coords = LocationUtils.geocodeAddress(context, homeAddress)
                            if (coords != null) {
                                homeLatitude = coords.first
                                homeLongitude = coords.second
                            }
                            geocoding = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = homeAddress.isNotBlank() && !geocoding
            ) {
                Text(if (geocoding) "Geocoding..." else "Get Coordinates from Address")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    currentProfile?.let { profile ->
                        viewModel.updateProfile(
                            profile.copy(
                                roomLength = roomLength.trim().takeIf { it.isNotEmpty() },
                                roomWidth = roomWidth.trim().takeIf { it.isNotEmpty() },
                                roomHeight = roomHeight.trim().takeIf { it.isNotEmpty() },
                                windowWidth = windowWidth.trim().takeIf { it.isNotEmpty() },
                                windowHeight = windowHeight.trim().takeIf { it.isNotEmpty() },
                                roomNotes = roomNotes.trim().takeIf { it.isNotEmpty() },
                                homeAddress = homeAddress.trim().takeIf { it.isNotEmpty() },
                                homeLatitude = homeLatitude,
                                homeLongitude = homeLongitude
                            )
                        )
                    }
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
