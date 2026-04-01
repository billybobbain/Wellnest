package com.billybobbain.wellnest.ui.screens

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.billybobbain.wellnest.WellnestViewModel
import com.billybobbain.wellnest.data.Appointment
import java.io.File
import java.io.FileOutputStream
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: WellnestViewModel,
    onNavigateBack: () -> Unit,
    onEditAppointment: (Long) -> Unit
) {
    // Use combined appointments (real + virtual recurring instances)
    val appointments by viewModel.allAppointmentsForCalendar.collectAsState()
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    val context = LocalContext.current

    // Group appointments by date
    val appointmentsByDate by remember {
        derivedStateOf {
            appointments
                .filter { !it.isArchived }
                .groupBy { appointment ->
                    Instant.ofEpochMilli(appointment.dateTime)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        generatePdf(context, currentMonth, appointmentsByDate)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Generate PDF")
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
            // Month navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
                }

                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
                }
            }

            // Day headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid
            MonthGrid(
                month = currentMonth,
                selectedDate = selectedDate,
                appointmentsByDate = appointmentsByDate,
                onDateClick = { date ->
                    selectedDate = if (selectedDate == date) null else date
                },
                modifier = Modifier.weight(1f)
            )

            HorizontalDivider()

            // Appointments for selected date
            if (selectedDate != null) {
                val dateAppointments = appointmentsByDate[selectedDate] ?: emptyList()

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = selectedDate!!.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (dateAppointments.isEmpty()) {
                        item {
                            Text(
                                text = "No appointments",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(dateAppointments.sortedBy { it.dateTime }) { appointment ->
                            AppointmentItem(
                                appointment = appointment,
                                onClick = { onEditAppointment(appointment.id) }
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Select a date to view appointments",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MonthGrid(
    month: YearMonth,
    selectedDate: LocalDate?,
    appointmentsByDate: Map<LocalDate, List<Appointment>>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstDayOfMonth = month.atDay(1)
    val lastDayOfMonth = month.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
    val today = LocalDate.now()

    Column(modifier = modifier) {
        // Calculate weeks needed
        val daysInMonth = month.lengthOfMonth()
        val totalCells = firstDayOfWeek + daysInMonth
        val weeksNeeded = (totalCells + 6) / 7

        repeat(weeksNeeded) { weekIndex ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                repeat(7) { dayIndex ->
                    val cellIndex = weekIndex * 7 + dayIndex
                    val dayOfMonth = cellIndex - firstDayOfWeek + 1

                    if (dayOfMonth in 1..daysInMonth) {
                        val date = month.atDay(dayOfMonth)
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val hasAppointments = appointmentsByDate.containsKey(date)
                        val appointmentCount = appointmentsByDate[date]?.size ?: 0

                        CalendarDay(
                            date = date,
                            isSelected = isSelected,
                            isToday = isToday,
                            hasAppointments = hasAppointments,
                            appointmentCount = appointmentCount,
                            onClick = { onDateClick(date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDay(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasAppointments: Boolean,
    appointmentCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    isToday -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surface
                },
                shape = MaterialTheme.shapes.small
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    isToday -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )

            if (hasAppointments) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    repeat(minOf(appointmentCount, 3)) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .padding(horizontal = 1.dp)
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.tertiary,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentItem(
    appointment: Appointment,
    onClick: () -> Unit
) {
    val time = remember(appointment.dateTime) {
        Instant.ofEpochMilli(appointment.dateTime)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(DateTimeFormatter.ofPattern("h:mm a"))
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            if (!appointment.icon.isNullOrEmpty()) {
                Text(
                    text = appointment.icon,
                    style = MaterialTheme.typography.displayMedium
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = appointment.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                if (!appointment.location.isNullOrEmpty()) {
                    Text(
                        text = appointment.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!appointment.notes.isNullOrEmpty()) {
                    Text(
                        text = appointment.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

fun generatePdf(
    context: Context,
    month: YearMonth,
    appointmentsByDate: Map<LocalDate, List<Appointment>>
) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(612, 792, 1).create() // 8.5x11 inches at 72 DPI
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    // Draw month title
    paint.textSize = 28f
    paint.textAlign = Paint.Align.CENTER
    paint.isFakeBoldText = true
    canvas.drawText(
        month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
        306f,
        50f,
        paint
    )

    // Draw day headers
    val dayHeaders = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    paint.textSize = 12f
    paint.isFakeBoldText = true
    val cellWidth = 80f
    val startX = 26f
    var y = 80f

    dayHeaders.forEachIndexed { index, day ->
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(day, startX + (index * cellWidth) + (cellWidth / 2), y, paint)
    }

    // Draw calendar grid
    paint.isFakeBoldText = false
    val cellHeight = 115f
    y = 95f

    val firstDayOfMonth = month.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
    val daysInMonth = month.lengthOfMonth()

    var currentDay = 1
    var weekRow = 0

    while (currentDay <= daysInMonth) {
        for (dayIndex in 0..6) {
            val cellX = startX + (dayIndex * cellWidth)
            val cellY = y + (weekRow * cellHeight)

            // Draw cell border
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(cellX, cellY, cellX + cellWidth, cellY + cellHeight, paint)

            // Draw day number if valid
            if ((weekRow == 0 && dayIndex >= firstDayOfWeek) || weekRow > 0) {
                if (currentDay <= daysInMonth) {
                    val date = month.atDay(currentDay)

                    // Draw day number
                    paint.style = Paint.Style.FILL
                    paint.textSize = 14f
                    paint.textAlign = Paint.Align.LEFT
                    canvas.drawText(currentDay.toString(), cellX + 5, cellY + 18, paint)

                    // Draw appointments for this day (max 2)
                    val dayAppointments = appointmentsByDate[date]
                    if (!dayAppointments.isNullOrEmpty()) {
                        paint.textSize = 16f
                        var appointmentY = cellY + 32f
                        val maxWidth = cellWidth - 10
                        var appointmentsDrawn = 0
                        val maxAppointments = 2

                        for (appointment in dayAppointments.take(maxAppointments)) {
                            // Check if we have room for at least 2 lines (time + title)
                            if (appointmentY + 40f > cellY + cellHeight - 5f) break

                            val time = Instant.ofEpochMilli(appointment.dateTime)
                                .atZone(ZoneId.systemDefault())
                                .toLocalTime()
                                .format(DateTimeFormatter.ofPattern("h:mm a"))

                            // Draw icon if present
                            var textStartX = cellX + 5
                            if (!appointment.icon.isNullOrEmpty()) {
                                paint.textSize = 16f
                                canvas.drawText(appointment.icon, textStartX, appointmentY, paint)
                                textStartX += paint.measureText(appointment.icon) + 2f
                            }

                            // Draw time (smaller to fit in cell)
                            paint.textSize = 11f
                            canvas.drawText(time, textStartX, appointmentY, paint)
                            appointmentY += 18f
                            textStartX = cellX + 5  // Reset for title
                            paint.textSize = 16f  // Reset for title

                            // Wrap title text
                            val title = appointment.title
                            val words = title.split(" ")
                            var currentLine = ""
                            var linesDrawn = 0
                            val maxLinesPerAppointment = 2

                            for (word in words) {
                                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                                if (paint.measureText(testLine) <= maxWidth) {
                                    currentLine = testLine
                                } else {
                                    // Draw current line and start new one
                                    canvas.drawText(currentLine, cellX + 5, appointmentY, paint)
                                    appointmentY += 20f
                                    linesDrawn++
                                    currentLine = word

                                    if (linesDrawn >= maxLinesPerAppointment) break
                                }
                            }

                            // Draw remaining text
                            if (currentLine.isNotEmpty() && linesDrawn < maxLinesPerAppointment) {
                                canvas.drawText(currentLine, cellX + 5, appointmentY, paint)
                                appointmentY += 20f
                            }

                            appointmentY += 8f // Space between appointments
                            appointmentsDrawn++
                        }

                        if (dayAppointments.size > appointmentsDrawn) {
                            canvas.drawText("+${dayAppointments.size - appointmentsDrawn} more", cellX + 5, appointmentY, paint)
                        }
                    }

                    currentDay++
                    if (currentDay > daysInMonth) break
                }
            }
        }
        weekRow++
    }

    pdfDocument.finishPage(page)

    // Save to file
    val fileName = "Calendar_${month.format(DateTimeFormatter.ofPattern("MMMM_yyyy"))}.pdf"
    val file = File(context.getExternalFilesDir(null), fileName)
    pdfDocument.writeTo(FileOutputStream(file))
    pdfDocument.close()

    // Share the PDF
    val uri = androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Calendar PDF"))
}
