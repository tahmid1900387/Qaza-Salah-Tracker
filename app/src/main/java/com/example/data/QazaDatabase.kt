package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [QazaPrayerEntity::class, QazaHistoryEntity::class, QazaSettingsEntity::class, TasbihEntity::class],
    version = 4,
    exportSchema = false
)
abstract class QazaDatabase : RoomDatabase() {
    abstract fun qazaDao(): QazaDao

    companion object {
        @Volatile
        private var INSTANCE: QazaDatabase? = null

        fun getDatabase(context: Context): QazaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QazaDatabase::class.java,
                    "qaza_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
