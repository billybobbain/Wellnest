package com.billybobbain.wellnest.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * To add a migration when changing the database schema:
 * 1. Increment the version number in @Database annotation
 * 2. Create a Migration object:
 *    val MIGRATION_X_Y = object : Migration(X, Y) {
 *        override fun migrate(database: SupportSQLiteDatabase) {
 *            database.execSQL("ALTER TABLE table_name ADD COLUMN column_name TYPE")
 *        }
 *    }
 * 3. Add it to the database builder: .addMigrations(MIGRATION_X_Y)
 */

@Database(
    entities = [
        Profile::class,
        Medication::class,
        Appointment::class,
        Contact::class,
        HealthProfile::class,
        InsuranceProvider::class,
        InsurancePolicy::class,
        SecurityCode::class,
        Settings::class,
        Supply::class,
        Message::class,
        Doctor::class,
        Location::class,
        DoctorLocation::class
    ],
    version = 9,
    exportSchema = false
)
abstract class WellnestDatabase : RoomDatabase() {
    abstract fun wellnestDao(): WellnestDao

    companion object {
        @Volatile
        private var INSTANCE: WellnestDatabase? = null

        // Migration from version 2 to 3: Add insurance card photo fields
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE insurance_policies ADD COLUMN frontCardPhotoUri TEXT"
                )
                database.execSQL(
                    "ALTER TABLE insurance_policies ADD COLUMN backCardPhotoUri TEXT"
                )
            }
        }

        // Migration from version 3 to 4: Add room dimension fields to profiles
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE profiles ADD COLUMN roomLength TEXT")
                database.execSQL("ALTER TABLE profiles ADD COLUMN roomWidth TEXT")
                database.execSQL("ALTER TABLE profiles ADD COLUMN roomHeight TEXT")
                database.execSQL("ALTER TABLE profiles ADD COLUMN windowWidth TEXT")
                database.execSQL("ALTER TABLE profiles ADD COLUMN windowHeight TEXT")
                database.execSQL("ALTER TABLE profiles ADD COLUMN roomNotes TEXT")
            }
        }

        // Migration from version 4 to 5: Add supplies table
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS supplies (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        profileId INTEGER NOT NULL,
                        itemName TEXT NOT NULL,
                        lastReplenished INTEGER,
                        notes TEXT,
                        FOREIGN KEY(profileId) REFERENCES profiles(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_supplies_profileId ON supplies(profileId)")
            }
        }

        // Migration from version 5 to 6: Add lastSelectedProfileId to settings
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE settings ADD COLUMN lastSelectedProfileId INTEGER")
            }
        }

        // Migration from version 6 to 7: Add messages table
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS messages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        profileId INTEGER NOT NULL,
                        originalText TEXT NOT NULL,
                        interpretedText TEXT,
                        timestamp INTEGER NOT NULL,
                        notes TEXT,
                        FOREIGN KEY(profileId) REFERENCES profiles(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_messages_profileId ON messages(profileId)")
            }
        }

        // Migration from version 7 to 8: Add doctors table and migrate medications/appointments
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Create doctors table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS doctors (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        specialty TEXT,
                        phone TEXT,
                        address TEXT,
                        notes TEXT
                    )
                    """.trimIndent()
                )

                // 2. Extract unique doctor names from medications and insert into doctors table
                database.execSQL(
                    """
                    INSERT INTO doctors (name)
                    SELECT DISTINCT prescribingDoctor
                    FROM medications
                    WHERE prescribingDoctor IS NOT NULL AND prescribingDoctor != ''
                    """.trimIndent()
                )

                // 3. Create new medications table with doctorId
                database.execSQL(
                    """
                    CREATE TABLE medications_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        profileId INTEGER NOT NULL,
                        drugName TEXT NOT NULL,
                        dosage TEXT,
                        frequency TEXT,
                        doctorId INTEGER,
                        pharmacy TEXT,
                        startDate INTEGER,
                        refillDate INTEGER,
                        notes TEXT,
                        classification TEXT,
                        diagnosis TEXT,
                        FOREIGN KEY(profileId) REFERENCES profiles(id) ON DELETE CASCADE,
                        FOREIGN KEY(doctorId) REFERENCES doctors(id) ON DELETE SET NULL
                    )
                    """.trimIndent()
                )

                // 4. Copy data from old medications table, linking doctorId
                database.execSQL(
                    """
                    INSERT INTO medications_new (id, profileId, drugName, dosage, frequency, doctorId, pharmacy, startDate, refillDate, notes, classification, diagnosis)
                    SELECT m.id, m.profileId, m.drugName, m.dosage, m.frequency, d.id, m.pharmacy, m.startDate, m.refillDate, m.notes, m.classification, m.diagnosis
                    FROM medications m
                    LEFT JOIN doctors d ON m.prescribingDoctor = d.name
                    """.trimIndent()
                )

                // 5. Drop old table and rename new one
                database.execSQL("DROP TABLE medications")
                database.execSQL("ALTER TABLE medications_new RENAME TO medications")

                // 6. Create indices for medications
                database.execSQL("CREATE INDEX IF NOT EXISTS index_medications_profileId ON medications(profileId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_medications_doctorId ON medications(doctorId)")

                // 7. Create new appointments table with doctorId
                database.execSQL(
                    """
                    CREATE TABLE appointments_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        profileId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        dateTime INTEGER NOT NULL,
                        location TEXT,
                        doctorId INTEGER,
                        notes TEXT,
                        reminderEnabled INTEGER NOT NULL,
                        reminderMinutesBefore INTEGER NOT NULL,
                        FOREIGN KEY(profileId) REFERENCES profiles(id) ON DELETE CASCADE,
                        FOREIGN KEY(doctorId) REFERENCES doctors(id) ON DELETE SET NULL
                    )
                    """.trimIndent()
                )

                // 8. Copy data from old appointments table
                database.execSQL(
                    """
                    INSERT INTO appointments_new (id, profileId, title, dateTime, location, doctorId, notes, reminderEnabled, reminderMinutesBefore)
                    SELECT id, profileId, title, dateTime, location, NULL, notes, reminderEnabled, reminderMinutesBefore
                    FROM appointments
                    """.trimIndent()
                )

                // 9. Drop old table and rename new one
                database.execSQL("DROP TABLE appointments")
                database.execSQL("ALTER TABLE appointments_new RENAME TO appointments")

                // 10. Create indices for appointments
                database.execSQL("CREATE INDEX IF NOT EXISTS index_appointments_profileId ON appointments(profileId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_appointments_doctorId ON appointments(doctorId)")
            }
        }

        // Migration from version 8 to 9: Add locations, home address, appointment improvements
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Add home address fields to profiles
                database.execSQL("ALTER TABLE profiles ADD COLUMN homeAddress TEXT")
                database.execSQL("ALTER TABLE profiles ADD COLUMN homeLatitude REAL")
                database.execSQL("ALTER TABLE profiles ADD COLUMN homeLongitude REAL")

                // 2. Create locations table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS locations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        address TEXT NOT NULL,
                        latitude REAL,
                        longitude REAL,
                        distanceMiles REAL,
                        phone TEXT,
                        notes TEXT
                    )
                    """.trimIndent()
                )

                // 3. Create doctor_locations join table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS doctor_locations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        doctorId INTEGER NOT NULL,
                        locationId INTEGER NOT NULL,
                        FOREIGN KEY(doctorId) REFERENCES doctors(id) ON DELETE CASCADE,
                        FOREIGN KEY(locationId) REFERENCES locations(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_doctor_locations_doctorId ON doctor_locations(doctorId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_doctor_locations_locationId ON doctor_locations(locationId)")

                // 4. Create new appointments table with additional fields
                database.execSQL(
                    """
                    CREATE TABLE appointments_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        profileId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        dateTime INTEGER NOT NULL,
                        location TEXT,
                        doctorId INTEGER,
                        locationId INTEGER,
                        notes TEXT,
                        reminderEnabled INTEGER NOT NULL,
                        reminderMinutesBefore INTEGER NOT NULL,
                        isArchived INTEGER NOT NULL DEFAULT 0,
                        milesDriven REAL,
                        wasAttended INTEGER NOT NULL DEFAULT 1,
                        FOREIGN KEY(profileId) REFERENCES profiles(id) ON DELETE CASCADE,
                        FOREIGN KEY(doctorId) REFERENCES doctors(id) ON DELETE SET NULL,
                        FOREIGN KEY(locationId) REFERENCES locations(id) ON DELETE SET NULL
                    )
                    """.trimIndent()
                )

                // 5. Copy data from old appointments table
                database.execSQL(
                    """
                    INSERT INTO appointments_new (id, profileId, title, dateTime, location, doctorId, locationId, notes, reminderEnabled, reminderMinutesBefore, isArchived, milesDriven, wasAttended)
                    SELECT id, profileId, title, dateTime, location, doctorId, NULL, notes, reminderEnabled, reminderMinutesBefore, 0, NULL, 1
                    FROM appointments
                    """.trimIndent()
                )

                // 6. Drop old table and rename new one
                database.execSQL("DROP TABLE appointments")
                database.execSQL("ALTER TABLE appointments_new RENAME TO appointments")

                // 7. Create indices for appointments
                database.execSQL("CREATE INDEX IF NOT EXISTS index_appointments_profileId ON appointments(profileId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_appointments_doctorId ON appointments(doctorId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_appointments_locationId ON appointments(locationId)")
            }
        }

        fun getDatabase(context: Context): WellnestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WellnestDatabase::class.java,
                    "wellnest_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
