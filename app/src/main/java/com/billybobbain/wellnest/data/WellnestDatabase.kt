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
        Message::class
    ],
    version = 7,
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

        fun getDatabase(context: Context): WellnestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WellnestDatabase::class.java,
                    "wellnest_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
