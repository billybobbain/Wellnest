# Wellnest

A comprehensive care management application for tracking health information, medications, appointments, and other important details for loved ones in assisted living.

![Wellnest Dashboard](screenshots/dashboard.png)

## Features (Phase 1 - MVP)

### Core Functionality
- **Multiple Profiles**: Manage care information for multiple people
- **Medications**: Track medications with dosage, frequency, prescribing doctor, pharmacy, and notes
- **Appointments**: Schedule appointments with date/time, location, and reminder notifications
- **Staff Contacts**: Store contact information for facility staff and caregivers
- **Health Profile**: Record vital health information including:
  - Height, weight, blood type
  - Allergies and medical conditions
  - Emergency contact information
- **Insurance Management**: Track insurance policies with:
  - Separate provider database for easy reuse
  - Policy numbers, member and provider phone numbers
  - Coverage type (PPO, HMO, etc.) and insurance type (Medical, Dental, Medicare, etc.)
- **Security Codes**: Store access codes for facility doors, rooms, etc.

### UI Features
- **6 Color Themes**: Teal, Purple, Blue, Green, Orange, Pink (with light/dark mode support)
- **Theme Persistence**: Selected theme is saved to database
- **Material Design 3**: Modern, clean interface
- **Intuitive Navigation**: Easy access to all features from main menu

## Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **Database**: Room (SQLite) with the following entities:
  - Profile
  - Medication
  - Appointment
  - Contact
  - HealthProfile
  - InsuranceProvider
  - InsurancePolicy
  - SecurityCode
  - Settings
- **State Management**: StateFlow for reactive UI updates
- **Navigation**: Jetpack Compose Navigation

## Building the Project

### Requirements
- Android Studio (latest version recommended)
- JDK 17 or later
- Android SDK with API 24+ (minimum) and API 36 (target)

### Build Instructions

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Run the app on an emulator or physical device

Or use the command line (requires JAVA_HOME to be set):
```bash
./gradlew assembleDebug
```

The APK will be generated in:
```
app/build/outputs/apk/debug/app-debug.apk
```

## Database Schema

All entities use foreign keys with cascade delete to maintain referential integrity. When a Profile is deleted, all associated data (medications, appointments, etc.) are automatically deleted.

### Key Relationships
- Profile (1) → (many) Medications
- Profile (1) → (many) Appointments
- Profile (1) → (many) Contacts
- Profile (1) → (one) HealthProfile
- Profile (1) → (many) InsurancePolicies
- Profile (1) → (many) SecurityCodes
- InsuranceProvider (1) → (many) InsurancePolicies

## Future Enhancements (Phase 2 & 3)

Planned features for future releases:
- Document/photo storage for insurance cards, medication bottles, etc.
- Notes/Journal for daily observations
- Task/Checklist system for care-related tasks
- Search functionality across all data
- History/Timeline views for tracking changes
- Export/Share functionality (PDF, text)
- Dashboard with quick access to upcoming appointments and current medications
- Enhanced appointment reminders with WorkManager notifications

## Package Structure

```
com.billybobbain.wellnest/
├── data/                     # Database entities and DAOs
│   ├── Profile.kt
│   ├── Medication.kt
│   ├── Appointment.kt
│   ├── Contact.kt
│   ├── HealthProfile.kt
│   ├── InsuranceProvider.kt
│   ├── InsurancePolicy.kt
│   ├── SecurityCode.kt
│   ├── Settings.kt
│   ├── WellnestDao.kt
│   ├── WellnestDatabase.kt
│   └── WellnestRepository.kt
├── ui/
│   ├── screens/              # All screen composables
│   └── theme/                # Theme, Color, Type definitions
├── MainActivity.kt
├── WellnestApp.kt           # Navigation setup
├── WellnestViewModel.kt     # Main ViewModel
└── Navigation.kt            # Screen routes

```

## Notes

- Database uses `.fallbackToDestructiveMigration()` - schema changes will clear all data
- All phone numbers, addresses, and personal information are stored locally on device
- No cloud sync or backup in Phase 1 (all data is local only)

## Icon

The app features a custom nest+heart launcher icon representing the caring and nurturing nature of the application.

---

Built with ❤️ to help families care for their loved ones.
