package com.billybobbain.wellnest.utils

import com.billybobbain.wellnest.data.*
import java.util.Calendar

object TestDataGenerator {

    suspend fun generateTestProfile(
        profileDao: WellnestDao
    ): Long {
        // Create Bilbo Baggins profile
        val profileId = profileDao.insertProfile(
            Profile(
                name = "Bilbo Baggins",
                notes = "Resident of Bag End wing, enjoys elevenses and telling stories",
                roomLength = "18'",
                roomWidth = "12'",
                roomHeight = "7'",
                windowWidth = "48\"",
                windowHeight = "36\"",
                roomNotes = "Round door, very cozy. Prefers lower ceilings."
            )
        )

        // Health Profile
        profileDao.insertHealthProfile(
            HealthProfile(
                profileId = profileId,
                height = "3'6\"",
                weight = "95 lbs",
                bloodType = "O+",
                allergies = "Ring-related anxiety, Dragon fire",
                medicalConditions = "Wanderlust, Adventure-seeking tendencies, Excellent for his age",
                emergencyContact = "Frodo Baggins",
                emergencyPhone = "(555) NEPHEW",
                notes = "111 years old and still going strong! Loves second breakfast. Primary Physician: Dr. Gandalf Grey (555) WIZARD"
            )
        )

        // Medications
        val medications = listOf(
            Medication(
                profileId = profileId,
                drugName = "Lembas Bread Vitamins",
                dosage = "One small piece",
                frequency = "Daily with elevenses",
                prescribingDoctor = "Dr. Elrond",
                pharmacy = "Rivendell Apothecary",
                notes = "Elvish recipe. Very sustaining.",
                classification = "Supplement"
            ),
            Medication(
                profileId = profileId,
                drugName = "Old Toby's Respiratory Support",
                dosage = "As needed",
                frequency = "After dinner",
                prescribingDoctor = "Dr. Gandalf Grey",
                pharmacy = "The Shire Pharmacy",
                notes = "For pipe smokers. The finest pipe-weed in the South Farthing.",
                classification = "Respiratory"
            ),
            Medication(
                profileId = profileId,
                drugName = "Mithril Joint Support",
                dosage = "1 tablet",
                frequency = "Twice daily",
                prescribingDoctor = "Dr. Gimli",
                pharmacy = "Lonely Mountain Pharmacy",
                notes = "Dwarven formula. Keeps joints strong for long journeys.",
                classification = "Anti-inflammatory"
            ),
            Medication(
                profileId = profileId,
                drugName = "Lisinopril",
                dosage = "10mg",
                frequency = "Once daily",
                prescribingDoctor = "Dr. Gandalf Grey",
                pharmacy = "Rivendell Apothecary",
                notes = "For blood pressure",
                classification = "Cardiovascular"
            ),
            Medication(
                profileId = profileId,
                drugName = "Metformin",
                dosage = "500mg",
                frequency = "Twice daily with meals",
                prescribingDoctor = "Dr. Gandalf Grey",
                pharmacy = "Rivendell Apothecary",
                notes = "With breakfast and dinner",
                classification = "Diabetes"
            )
        )
        medications.forEach { profileDao.insertMedication(it) }

        // Appointments
        val calendar = Calendar.getInstance()

        calendar.add(Calendar.DAY_OF_MONTH, 3)
        profileDao.insertAppointment(
            Appointment(
                profileId = profileId,
                title = "Annual Check-up with Gandalf",
                dateTime = calendar.timeInMillis,
                location = "Rivendell Medical Center",
                notes = "Bring list of current adventures and concerns"
            )
        )

        calendar.add(Calendar.DAY_OF_MONTH, 7)
        profileDao.insertAppointment(
            Appointment(
                profileId = profileId,
                title = "Cardiology Follow-up",
                dateTime = calendar.timeInMillis,
                location = "Rivendell Medical Center - Dr. Elrond",
                notes = "Heart health check after recent journey"
            )
        )

        calendar.add(Calendar.DAY_OF_MONTH, 14)
        profileDao.insertAppointment(
            Appointment(
                profileId = profileId,
                title = "Pharmacy Consultation",
                dateTime = calendar.timeInMillis,
                location = "The Shire Pharmacy",
                notes = "Review current medications and pipe-weed usage"
            )
        )

        // Contacts
        val contacts = listOf(
            Contact(
                profileId = profileId,
                name = "Gandalf the Grey",
                role = "Primary Physician",
                phone = "(555) WIZARD1",
                email = "gandalf@rivendell-medical.org",
                notes = "A wizard is never late. Prefers early morning appointments."
            ),
            Contact(
                profileId = profileId,
                name = "Frodo Baggins",
                role = "Nephew / Emergency Contact",
                phone = "(555) NEPHEW1",
                email = "frodo@bagend.shire",
                notes = "Adopted heir. Lives nearby in Bag End East wing."
            ),
            Contact(
                profileId = profileId,
                name = "Samwise Gamgee",
                role = "Daily Caregiver",
                phone = "(555) GARDENER",
                email = "sam@bagend-gardens.shire",
                notes = "Former gardener, now assists with daily care. Excellent cook."
            ),
            Contact(
                profileId = profileId,
                name = "Nurse Took",
                role = "Facility Nurse",
                phone = "(555) NURSING",
                email = "nurse.took@bagend-living.com",
                notes = "On duty Mon-Fri 8am-4pm. From the Took family."
            ),
            Contact(
                profileId = profileId,
                name = "Elrond Half-elven",
                role = "Cardiologist",
                phone = "(555) ELVEN-1",
                email = "elrond@rivendell-medical.org",
                notes = "Specialist in longevity and heart health. Very wise."
            )
        )
        contacts.forEach { profileDao.insertContact(it) }

        // Insurance
        val insuranceProviderId = profileDao.insertInsuranceProvider(
            InsuranceProvider(
                name = "Middle Earth Medicare",
                address = "123 Shire Lane, Hobbiton",
                notes = "Website: www.middle-earth-medicare.org | Phone: (555) INSURE1"
            )
        )

        profileDao.insertInsurancePolicy(
            InsurancePolicy(
                profileId = profileId,
                providerId = insuranceProviderId,
                policyNumber = "HOBBIT-111-SHIRE",
                providerPhone = "(555) INSURE1",
                memberPhone = "(555) MEMBER1",
                coverageType = "PPO",
                insuranceType = "Medicare",
                notes = "The 'There and Back Again' premium plan. Member#: BAGGINS-1111, Group#: SHIRE-FOLK-01. Covers adventure-related injuries."
            )
        )

        // Security Codes
        val securityCodes = listOf(
            SecurityCode(
                profileId = profileId,
                label = "Main Gate Code",
                code = "MELLON",
                notes = "Elvish for 'friend'. Speak it and enter."
            ),
            SecurityCode(
                profileId = profileId,
                label = "Room 111",
                code = "1111",
                notes = "His age! Easy to remember."
            ),
            SecurityCode(
                profileId = profileId,
                label = "Medicine Cabinet",
                code = "PRECIOUS",
                notes = "For safekeeping the ring... er, medications."
            )
        )
        securityCodes.forEach { profileDao.insertSecurityCode(it) }

        // Supplies
        val currentTime = System.currentTimeMillis()
        val oneDayAgo = currentTime - (24 * 60 * 60 * 1000)
        val twoDaysAgo = currentTime - (2 * 24 * 60 * 60 * 1000)
        val threeDaysAgo = currentTime - (3 * 24 * 60 * 60 * 1000)

        val supplies = listOf(
            Supply(
                profileId = profileId,
                itemName = "Lembas Bread",
                lastReplenished = oneDayAgo,
                notes = "From Lothlórien. One piece is enough for a day's march."
            ),
            Supply(
                profileId = profileId,
                itemName = "Old Toby Pipe-weed",
                lastReplenished = twoDaysAgo,
                notes = "South Farthing's finest. For evening relaxation."
            ),
            Supply(
                profileId = profileId,
                itemName = "Second Breakfast Supplies",
                lastReplenished = threeDaysAgo,
                notes = "Eggs, bacon, sausages, mushrooms, tomatoes. The essentials!"
            ),
            Supply(
                profileId = profileId,
                itemName = "Seed Cake",
                lastReplenished = currentTime,
                notes = "For afternoon tea. Recipe from the Old Took."
            ),
            Supply(
                profileId = profileId,
                itemName = "Milk",
                lastReplenished = oneDayAgo,
                notes = "From local Shire farms. Delivered fresh daily."
            )
        )
        supplies.forEach { profileDao.insertSupply(it) }

        return profileId
    }

    suspend fun deleteTestProfile(
        profileDao: WellnestDao,
        profileId: Long
    ) {
        val profile = profileDao.getProfile(profileId)
        profile.collect { p ->
            p?.let { profileDao.deleteProfile(it) }
        }
    }
}
