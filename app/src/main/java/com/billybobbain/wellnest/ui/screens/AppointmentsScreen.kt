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
import com.billybobbain.wellnest.data.Doctor
import com.billybobbain.wellnest.data.Location
import com.billybobbain.wellnest.data.RecurringAppointment
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
    val recurringAppointments by viewModel.recurringAppointments.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    val doctors by viewModel.doctors.collectAsState()
    val locations by viewModel.locations.collectAsState()
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
        if (appointments.isEmpty() && recurringAppointments.isEmpty()) {
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
                // Recurring appointments section
                if (recurringAppointments.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recurring Appointments",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(recurringAppointments) { recurring ->
                        RecurringAppointmentCard(
                            recurringAppointment = recurring,
                            doctors = doctors,
                            locations = locations,
                            onClick = { onEditAppointment(recurring.id) },
                            onDelete = { viewModel.deleteRecurringAppointment(recurring) }
                        )
                    }
                    item {
                        Divider(modifier = Modifier.padding(vertical = 16.dp))
                    }
                }

                // One-time appointments section
                if (appointments.isNotEmpty()) {
                    item {
                        Text(
                            text = "Scheduled Appointments",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(appointments) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            doctors = doctors,
                            locations = locations,
                            onClick = { onEditAppointment(appointment.id) },
                            onDelete = { viewModel.deleteAppointment(appointment) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentCard(
    appointment: Appointment,
    doctors: List<Doctor>,
    locations: List<Location>,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    val formattedDateTime = dateFormat.format(Date(appointment.dateTime))

    // Look up doctor and location names
    val doctorName = appointment.doctorId?.let { id -> doctors.find { it.id == id }?.name }
    val locationName = appointment.locationId?.let { id -> locations.find { it.id == id }?.name }

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
            // Icon
            if (!appointment.icon.isNullOrEmpty()) {
                Text(
                    text = appointment.icon,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }

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
                // Show doctor name if available
                if (doctorName != null) {
                    Text(
                        text = "Dr. $doctorName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                // Show location from Location entity if available, otherwise fall back to legacy string
                if (locationName != null) {
                    Text(
                        text = locationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (!appointment.location.isNullOrEmpty()) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringAppointmentCard(
    recurringAppointment: RecurringAppointment,
    doctors: List<Doctor>,
    locations: List<Location>,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    // Format time of day
    val hours = (recurringAppointment.timeOfDay / 3_600_000).toInt()
    val minutes = ((recurringAppointment.timeOfDay % 3_600_000) / 60_000).toInt()
    val amPm = if (hours < 12) "AM" else "PM"
    val displayHours = if (hours == 0) 12 else if (hours > 12) hours - 12 else hours
    val formattedTime = String.format("%d:%02d %s", displayHours, minutes, amPm)

    // Format days of week
    val dayNames = mapOf(1 to "Su", 2 to "Mo", 3 to "Tu", 4 to "We", 5 to "Th", 6 to "Fr", 7 to "Sa")
    val days = recurringAppointment.daysOfWeek.split(",").mapNotNull { it.toIntOrNull() }
    val formattedDays = days.mapNotNull { dayNames[it] }.joinToString(", ")

    // Look up doctor and location names
    val doctorName = recurringAppointment.doctorId?.let { id -> doctors.find { it.id == id }?.name }
    val locationName = recurringAppointment.locationId?.let { id -> locations.find { it.id == id }?.name }

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
            // Icon
            if (!recurringAppointment.icon.isNullOrEmpty()) {
                Text(
                    text = recurringAppointment.icon,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = recurringAppointment.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "⟳",  // Recurring symbol
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Every $formattedDays at $formattedTime",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Show doctor name if available
                if (doctorName != null) {
                    Text(
                        text = "Dr. $doctorName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                // Show location from Location entity if available, otherwise fall back to legacy string
                if (locationName != null) {
                    Text(
                        text = locationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (!recurringAppointment.location.isNullOrEmpty()) {
                    Text(
                        text = recurringAppointment.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (recurringAppointment.reminderEnabled) {
                    Text(
                        text = "Reminder: ${recurringAppointment.reminderMinutesBefore} min before",
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
