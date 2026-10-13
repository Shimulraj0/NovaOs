package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CommandLogEntity::class, RoutineEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NovaDatabase : RoomDatabase() {
    abstract fun novaDao(): NovaDao

    companion object {
        @Volatile
        private var INSTANCE: NovaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): NovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NovaDatabase::class.java,
                    "novaos_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialRoutines(database.novaDao())
                    }
                }
            }
        }

        private suspend fun populateInitialRoutines(dao: NovaDao) {
            val presets = listOf(
                RoutineEntity(
                    title = "Sleep & Night Routine",
                    triggerPhrase = "good night",
                    description = "Mutes media audio, checks battery status, and opens Clock for alarms",
                    icon = "bedtime",
                    taskTypesJson = "MUTE_VOLUME,CHECK_BATTERY,OPEN_APP_CLOCK",
                    isSystemPreset = true
                ),
                RoutineEntity(
                    title = "Work Focus Mode",
                    triggerPhrase = "focus time",
                    description = "Silences ring volume and presents system diagnostics",
                    icon = "psychology",
                    taskTypesJson = "MUTE_RING,CHECK_STORAGE,DIAGNOSTIC_REPORT",
                    isSystemPreset = true
                ),
                RoutineEntity(
                    title = "Quick Prep / Meeting",
                    triggerPhrase = "meeting prep",
                    description = "Mutes all media volume and copies system status to clipboard",
                    icon = "groups",
                    taskTypesJson = "MUTE_VOLUME,COPY_DIAGNOSTICS",
                    isSystemPreset = true
                ),
                RoutineEntity(
                    title = "Morning Kickoff",
                    triggerPhrase = "good morning",
                    description = "Unmutes volume to 50%, runs battery check, and inspects storage",
                    icon = "wb_sunny",
                    taskTypesJson = "VOLUME_50,CHECK_BATTERY,CHECK_STORAGE",
                    isSystemPreset = true
                )
            )
            dao.insertRoutines(presets)
        }
    }
}
