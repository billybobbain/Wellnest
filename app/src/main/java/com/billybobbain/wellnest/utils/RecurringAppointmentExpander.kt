package com.billybobbain.wellnest.utils

import com.billybobbain.wellnest.data.Appointment
import com.billybobbain.wellnest.data.RecurringAppointment
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Utility for expanding recurring appointments into virtual appointment instances.
 *
 * Virtual instances have negative IDs to distinguish them from real appointments.
 * The formula -(recurringId * 10000 + dayOfYear) ensures uniqueness.
 */
object RecurringAppointmentExpander {

    /**
     * Expands a single recurring appointment into virtual Appointment instances for a date range.
     *
     * @param recurring The recurring appointment pattern
     * @param startDate Start of range (inclusive)
     * @param endDate End of range (inclusive)
     * @return List of virtual Appointment instances with negative IDs
     */
    fun expandRecurringAppointment(
        recurring: RecurringAppointment,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Appointment> {
        if (!recurring.isActive) {
            return emptyList()
        }

        val instances = mutableListOf<Appointment>()
        val daysOfWeek = recurring.daysOfWeek.split(",").mapNotNull { it.toIntOrNull() }

        if (daysOfWeek.isEmpty()) {
            return emptyList()
        }

        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val dayOfWeek = currentDate.dayOfWeek.value // 1=Monday, 7=Sunday
            val adjustedDay = if (dayOfWeek == 7) 1 else dayOfWeek + 1 // Convert to 1=Sunday, 7=Saturday

            if (adjustedDay in daysOfWeek) {
                // Create virtual instance for this date
                val dateTime = currentDate.atTime(LocalTime.ofNanoOfDay(recurring.timeOfDay * 1_000_000))
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                // Use negative ID based on recurring ID and day of year for uniqueness
                val virtualId = -(recurring.id * 10000 + currentDate.dayOfYear)

                instances.add(
                    Appointment(
                        id = virtualId,
                        profileId = recurring.profileId,
                        title = recurring.title,
                        dateTime = dateTime,
                        location = recurring.location,
                        doctorId = recurring.doctorId,
                        locationId = recurring.locationId,
                        notes = recurring.notes,
                        reminderEnabled = recurring.reminderEnabled,
                        reminderMinutesBefore = recurring.reminderMinutesBefore,
                        isArchived = false,
                        milesDriven = null,
                        wasAttended = true,
                        icon = recurring.icon
                    )
                )
            }

            currentDate = currentDate.plusDays(1)
        }

        return instances
    }

    /**
     * Expands all recurring appointments for a date range.
     *
     * @param recurringList List of recurring appointment patterns
     * @param startDate Start of range (inclusive)
     * @param endDate End of range (inclusive)
     * @return List of all virtual Appointment instances
     */
    fun expandAllRecurring(
        recurringList: List<RecurringAppointment>,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Appointment> {
        return recurringList.flatMap { recurring ->
            expandRecurringAppointment(recurring, startDate, endDate)
        }
    }

    /**
     * Checks if an appointment ID represents a virtual recurring instance.
     *
     * @param appointmentId The appointment ID to check
     * @return True if this is a virtual recurring instance (negative ID)
     */
    fun isVirtualInstance(appointmentId: Long): Boolean {
        return appointmentId < 0
    }

    /**
     * Extracts the recurring appointment ID from a virtual instance ID.
     *
     * @param virtualId The virtual instance ID (must be negative)
     * @return The original recurring appointment ID, or null if not a virtual instance
     */
    fun getRecurringIdFromVirtual(virtualId: Long): Long? {
        if (virtualId >= 0) return null
        return (-virtualId) / 10000
    }
}
