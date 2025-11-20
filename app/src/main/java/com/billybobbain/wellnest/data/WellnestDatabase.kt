package com.billybobbain.wellnest.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

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
        Settings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WellnestDatabase : RoomDatabase() {
    abstract fun wellnestDao(): WellnestDao

    companion object {
        @Volatile
        private var INSTANCE: WellnestDatabase? = null

        fun getDatabase(context: Context): WellnestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WellnestDatabase::class.java,
                    "wellnest_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
