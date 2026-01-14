# Wellnest

A comprehensive care management application for tracking health information, medications, appointments, and other important details for loved ones in assisted living.

## Features

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
  - Photo storage for insurance card front/back
- **Security Codes**: Store access codes for facility doors, rooms, etc.
- **Room Information**: Track room dimensions and details:
  - Room length, width, and ceiling height
  - Window dimensions for drapes/curtains
  - Room notes for additional details
  - Perfect for furniture shopping and planning
- **Supply Tracking**: Simple consumables tracker:
  - Track items that need regular replenishing (milk, snacks, etc.)
  - Last replenished date tracking
  - Quick "Mark as Replenished" button
  - No alerts - just a reference list
- **Message Clarification** (Optional): AI-powered message clarification for hard-to-read text messages:
  - Helps understand garbled or fragmented messages from loved ones with vision problems
  - Bring Your Own Key (BYOK) - users provide their own Claude API key
  - Secure encrypted storage for API keys (AES256-GCM with hardware-backed encryption)
  - Uses Claude Haiku model (~$0.0002 per message, ~25,000 messages on free tier)
  - Shows clarified messages by default in the messages list
  - Completely optional - app works fully without this feature
  - Privacy-focused: your API key only communicates with Claude API

### UI Features
- **6 Color Themes**: Teal, Purple, Blue, Green, Orange, Pink (with light/dark mode support)
- **Theme Persistence**: Selected theme is saved to database
- **Material Design 3**: Modern, clean interface
- **Intuitive Navigation**: Easy access to all features from main menu

## Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **Database**: Room (SQLite) version 7 with the following entities:
  - Profile (with room dimension fields)
  - Medication
  - Appointment
  - Contact
  - HealthProfile
  - InsuranceProvider
  - InsurancePolicy (with photo URI fields)
  - SecurityCode
  - Supply
  - Message (with original and clarified text)
  - Settings (with last selected profile and theme preference)
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
- Profile (1) → (many) Supplies
- Profile (1) → (many) Messages
- InsuranceProvider (1) → (many) InsurancePolicies

### Database Migrations
The database uses explicit migrations to preserve user data across schema changes:
- v2→v3: Added insurance card photo fields
- v3→v4: Added room dimension fields to profiles
- v4→v5: Created supplies table
- v5→v6: Added lastSelectedProfileId to settings (persists profile selection across app restarts)
- v6→v7: Created messages table for message clarification feature

## Screenshots

### Profile Selection
Choose which loved one's information to view and manage.

![Profile Selection](screenshots/select-profile.png)

### Main Dashboard
Quick access to all features from a clean, organized home screen.

![Dashboard](screenshots/dashboard.png)

### Medications
Track all medications with dosage, frequency, prescribing doctor, and pharmacy information.

![Medications List](screenshots/medications.png)

### Appointments
Keep track of upcoming appointments with date, time, location, and notes.

![Appointments](screenshots/appointments.png)

![Edit Appointment](screenshots/edit-appointment.png)

### Staff & Caregiver Contacts
Store contact information for facility staff, doctors, and caregivers.

![Contacts List](screenshots/contacts.png)

![Edit Contact](screenshots/edit-contact.png)

### Health Profile
Record vital health information including allergies, medical conditions, and emergency contacts.

![Health Profile](screenshots/health-profile.png)

### Insurance Information
Manage insurance policies with provider details, policy numbers, and card photos.

![Insurance Policies](screenshots/insurance.png)

![Edit Insurance Policy](screenshots/edit-insurance.png)

### Room Information
Track room dimensions for furniture shopping, including window measurements for drapes.

![Edit Room Information](screenshots/edit-room-information.png)

### Supply Tracking
Keep track of consumables that need regular replenishing like milk, snacks, and personal items.

![Supplies List](screenshots/supplies.png)

![Edit Supply](screenshots/edit-supply.png)

### Security Codes
Securely store access codes for facility doors, rooms, and cabinets.

![Security Codes](screenshots/security-codes.png)

![Edit Security Code](screenshots/edit-security-code.png)

### Profile Management
Edit profile information including name, photo, notes, and room details.

![Edit Profile](screenshots/edit-profile.png)

## Future Enhancements

See `FEATURE_IDEAS.md` for detailed planning and priorities.

### Under Consideration
- **Meal/Menu Tracking** - Import facility menus, track meals eaten together
- **Calendar Coordination** - Shared calendaring for family members coordinating visits
- **Social/Community Features** - Optional message boards for caregivers at same facility
- **Search functionality** - Search across all data
- **Export/Share functionality** - PDF, text export
- **Enhanced appointment reminders** - WorkManager notifications

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
│   ├── Supply.kt
│   ├── Message.kt
│   ├── Settings.kt
│   ├── WellnestDao.kt
│   ├── WellnestDatabase.kt
│   └── WellnestRepository.kt
├── ui/
│   ├── screens/              # All screen composables
│   │   ├── MessagesScreen.kt
│   │   ├── AddEditMessageScreen.kt
│   │   ├── SettingsScreen.kt
│   │   └── ...
│   └── theme/                # Theme, Color, Type definitions
├── utils/                    # Utility classes
│   ├── ImageUtils.kt
│   ├── MedicationImporter.kt
│   ├── ClaudeApiService.kt   # AI message clarification service
│   └── EncryptedPrefsManager.kt  # Secure API key storage
├── MainActivity.kt
├── WellnestApp.kt           # Navigation setup
├── WellnestViewModel.kt     # Main ViewModel
└── Navigation.kt            # Screen routes

```

## Notes

- Database uses explicit migrations to preserve user data across schema changes
- All phone numbers, addresses, and personal information are stored locally on device
- No cloud sync or backup - all data is local only
- Profile photos and insurance card images stored in app-private storage
- **AI Clarification Privacy**: When enabled, only the message text and your API key are sent to Claude API. No other personal information is transmitted. Your API key is stored encrypted using hardware-backed AES256-GCM encryption.

## Icon

The app features a custom nest+heart launcher icon representing the caring and nurturing nature of the application.

---

Built with ❤️ to help families care for their loved ones.
