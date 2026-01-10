package com.billybobbain.wellnest.utils

import com.billybobbain.wellnest.data.Medication
import java.text.SimpleDateFormat
import java.util.Locale

object MedicationImporter {

    /**
     * Parse CSV and return list of medications
     * Expected CSV format:
     * Medication,Strength,Type,Route,Frequency,Instruction,Indication,Schedule,Prescriber,Date
     */
    fun parseCsv(csvContent: String, profileId: Long): Result<List<Medication>> {
        return try {
            val lines = csvContent.trim().lines()
            if (lines.isEmpty()) {
                return Result.failure(Exception("CSV is empty"))
            }

            // Skip header row
            val dataLines = lines.drop(1).filter { it.isNotBlank() }

            val medications = dataLines.mapNotNull { line ->
                parseCsvLine(line, profileId)
            }

            Result.success(medications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseCsvLine(line: String, profileId: Long): Medication? {
        return try {
            // Parse CSV properly handling quoted fields
            val parts = parseCsvFields(line)

            if (parts.size < 10) {
                // Skip malformed lines
                return null
            }

            val medication = parts[0]
            val strength = parts[1]
            val type = parts[2]
            val route = parts[3]
            val frequency = parts[4]
            val instruction = parts[5]
            val indication = parts[6]
            val schedule = parts[7]
            val prescriber = parts[8]
            val dateStr = parts[9]

            // Combine strength, type, and route for dosage
            val dosage = buildString {
                append(strength)
                if (type.isNotBlank()) append(" $type")
                if (route.isNotBlank()) append(" ($route)")
            }

            // Parse date (MM/DD/YY format)
            val startDate = parseDateString(dateStr)

            // Build classification from schedule if present
            val classification = if (schedule.isNotBlank()) {
                "Schedule $schedule"
            } else null

            // Combine instruction with type/route info in notes
            val notes = buildString {
                if (instruction.isNotBlank()) {
                    append(instruction)
                }
                if (type.isNotBlank() || route.isNotBlank()) {
                    if (isNotEmpty()) append("\n")
                    append("Type: $type, Route: $route")
                }
            }.takeIf { it.isNotBlank() }

            Medication(
                profileId = profileId,
                drugName = medication,
                dosage = dosage.takeIf { it.isNotBlank() },
                frequency = frequency.takeIf { it.isNotBlank() },
                prescribingDoctor = prescriber.takeIf { it.isNotBlank() },
                pharmacy = null, // Not in CSV
                startDate = startDate,
                refillDate = null, // Not in CSV
                notes = notes,
                classification = classification,
                diagnosis = indication.takeIf { it.isNotBlank() }
            )
        } catch (e: Exception) {
            null // Skip problematic lines
        }
    }

    private fun parseDateString(dateStr: String): Long? {
        return try {
            if (dateStr.isBlank()) return null

            // Try MM/DD/YY format
            val dateFormat = SimpleDateFormat("MM/dd/yy", Locale.US)
            dateFormat.parse(dateStr)?.time
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse CSV fields properly handling quoted values
     */
    private fun parseCsvFields(line: String): List<String> {
        val fields = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false

        for (i in line.indices) {
            val char = line[i]

            when {
                char == '"' -> {
                    inQuotes = !inQuotes
                }
                char == ',' && !inQuotes -> {
                    fields.add(currentField.toString().trim())
                    currentField.clear()
                }
                else -> {
                    currentField.append(char)
                }
            }
        }

        // Add the last field
        fields.add(currentField.toString().trim())

        return fields
    }
}
