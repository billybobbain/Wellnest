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
import com.billybobbain.wellnest.data.Appointment
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    viewModel: WellnestViewModel,
    onNavigateBack: () -> Unit,
    onAddAppointment: () -> Unit,
    onEditAppointment: (Long) -> Unit
) {
    val appointments by viewModel.appointments.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointments") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val shareText = formatAppointmentsForSharing(appointments, currentProfile?.name ?: "Patient")
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                putExtra(Intent.EXTRA_SUBJECT, "Appointment Schedule")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Appointments"))
                        },
                        enabled = appointments.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Appointments")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAppointment) {
                Icon(Icons.Default.Add, contentDescription = "Add Appointment")
            }
        }
    ) { padding ->
        if (appointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No appointments yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(appointments) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onClick = { onEditAppointment(appointment.id) },
                        onDelete = { viewModel.deleteAppointment(appointment) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentCard(
    appointment: Appointment,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    val formattedDateTime = dateFormat.format(Date(appointment.dateTime))

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
                    text = appointment.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formattedDateTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!appointment.location.isNullOrEmpty()) {
                    Text(
                        text = appointment.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (appointment.reminderEnabled) {
                    Text(
                        text = "Reminder: ${appointment.reminderMinutesBefore} min before",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
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
 * Format appointments list for sharing
 */
private fun formatAppointmentsForSharing(appointments: List<Appointment>, profileName: String): String {
    if (appointments.isEmpty()) {
        return "No appointments to share"
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
    val currentDate = SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date())

    // Sort appointments by date
    val sortedAppointments = appointments.sortedBy { it.dateTime }

    return buildString {
        appendLine("APPOINTMENT SCHEDULE")
        appendLine("Patient: $profileName")
        appendLine("Generated: $currentDate")
        appendLine()
        appendLine("=".repeat(40))
        appendLine()

        sortedAppointments.forEachIndexed { index, apt ->
            val date = Date(apt.dateTime)
            appendLine("${index + 1}. ${apt.title}")
            appendLine("   Date: ${dateFormat.format(date)}")
            appendLine("   Time: ${timeFormat.format(date)}")

            if (!apt.location.isNullOrEmpty()) {
                appendLine("   Location: ${apt.location}")
            }

            if (!apt.notes.isNullOrEmpty()) {
                appendLine("   Notes: ${apt.notes}")
            }

            if (apt.reminderEnabled) {
                appendLine("   Reminder: ${apt.reminderMinutesBefore} minutes before")
            }

            appendLine()
        }

        appendLine("=".repeat(40))
        appendLine("Total: ${appointments.size} appointment(s)")
    }
}
