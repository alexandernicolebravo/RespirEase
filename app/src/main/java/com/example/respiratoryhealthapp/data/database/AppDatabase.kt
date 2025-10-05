package com.example.respiratoryhealthapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.respiratoryhealthapp.data.dao.BreathingExerciseDao
import com.example.respiratoryhealthapp.data.dao.DoctorNoteDao
import com.example.respiratoryhealthapp.data.dao.ReminderDao
import com.example.respiratoryhealthapp.data.dao.SymptomDao
import com.example.respiratoryhealthapp.data.dao.TestResultDao
import com.example.respiratoryhealthapp.data.model.BreathingExercise
import com.example.respiratoryhealthapp.data.model.DoctorNote
import com.example.respiratoryhealthapp.data.model.Reminder
import com.example.respiratoryhealthapp.data.model.RespiratoryCondition
import com.example.respiratoryhealthapp.data.model.Symptom
import com.example.respiratoryhealthapp.data.model.TestResult
import com.example.respiratoryhealthapp.data.repository.UserProfileRepository
import com.example.respiratoryhealthapp.notifications.AndroidAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Database(entities = [Symptom::class, BreathingExercise::class, Reminder::class, TestResult::class, DoctorNote::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class, BreathingExerciseTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun symptomDao(): SymptomDao
    abstract fun breathingExerciseDao(): BreathingExerciseDao
    abstract fun reminderDao(): ReminderDao
    abstract fun testResultDao(): TestResultDao
    abstract fun doctorNoteDao(): DoctorNoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "respiratory_health_db"
                )
                .addCallback(DatabaseCallback(context.applicationContext))
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration(false) // Set to false to prevent losing data
                .build()
                INSTANCE = instance
                instance
            }
        }

        // Define migrations between versions
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example migration: Add a severity column to symptoms
                database.execSQL("ALTER TABLE symptoms ADD COLUMN severity INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example: Add isActive flag to reminders
                database.execSQL("ALTER TABLE reminders ADD COLUMN is_active INTEGER NOT NULL DEFAULT 1")
            }
        }
        
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example: Add notes field to test results
                database.execSQL("ALTER TABLE test_results ADD COLUMN notes TEXT DEFAULT NULL")
            }
        }
        
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example: Restructure doctor notes table
                database.execSQL("CREATE TABLE doctor_notes_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, content TEXT NOT NULL, date INTEGER NOT NULL, doctor_name TEXT, appointment_location TEXT, has_attachment INTEGER NOT NULL DEFAULT 0, attachment_uri TEXT)")
                database.execSQL("INSERT INTO doctor_notes_new (id, title, content, date) SELECT id, title, content, date FROM doctor_notes")
                database.execSQL("DROP TABLE doctor_notes")
                database.execSQL("ALTER TABLE doctor_notes_new RENAME TO doctor_notes")
            }
        }

        // Database callback for initialization
        private class DatabaseCallback(private val context: Context) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        populateInitialBreathingExercises(database.breathingExerciseDao())
                        populateInitialReminders(database.reminderDao())
                        scheduleInitialActiveReminders(database.reminderDao(), context)
                    }
                }
            }

            suspend fun populateInitialBreathingExercises(breathingExerciseDao: BreathingExerciseDao) {
                if (breathingExerciseDao.getExercises().firstOrNull()?.isNotEmpty() == true) {
                    return
                }

                val sampleExercises = listOf(
                    BreathingExercise(
                        name = "Pursed Lip Breathing",
                        description = "Helps slow your breathing down, making each breath more effective.",
                        instructions = listOf(
                            "Relax your neck and shoulder muscles.",
                            "Breathe in slowly through your nose for two counts, keeping your mouth closed.",
                            "Pucker or purse your lips as if you were going to whistle.",
                            "Breathe out slowly and gently through your pursed lips for four counts."
                        ),
                        durationMinutes = 5,
                        targetConditions = listOf(RespiratoryCondition.COPD, RespiratoryCondition.ASTHMA, RespiratoryCondition.GENERAL_WELLNESS),
                        benefits = listOf("Improves lung mechanics and breathing pattern", "Reduces shortness of breath", "Promotes relaxation")
                    ),
                    BreathingExercise(
                        name = "Diaphragmatic Breathing (Belly Breathing)",
                        description = "Strengthens the diaphragm, a large muscle located at the base of the lungs that is the most efficient muscle of breathing.",
                        instructions = listOf(
                            "Lie on your back on a flat surface or in a bed, with your knees bent and your head supported.",
                            "Place one hand on your upper chest and the other just below your rib cage. This will allow you to feel your diaphragm move as you breathe.",
                            "Breathe in slowly through your nose so that your stomach moves out against your hand. The hand on your chest should remain as still as possible.",
                            "Tighten your stomach muscles, letting them fall inward as you exhale through pursed lips. The hand on your upper chest must remain as still as possible."
                        ),
                        durationMinutes = 10,
                        targetConditions = listOf(RespiratoryCondition.ASTHMA, RespiratoryCondition.COPD, RespiratoryCondition.GENERAL_WELLNESS),
                        benefits = listOf("Strengthens the diaphragm", "Decreases oxygen demand", "Uses less effort and energy to breathe")
                    ),
                    BreathingExercise(
                        name = "Box Breathing (Square Breathing)",
                        description = "A simple technique to calm the nervous system and reduce stress.",
                        instructions = listOf(
                            "Exhale to a count of four.",
                            "Hold your lungs empty for a count of four.",
                            "Inhale to a count of four.",
                            "Hold air in your lungs for a count of four."
                        ),
                        durationMinutes = 5,
                        targetConditions = listOf(RespiratoryCondition.GENERAL_WELLNESS, RespiratoryCondition.ALLERGIC_RHINITIS),
                        videoUrl = "https://www.youtube.com/watch?v=tEmt1Znux58",
                        benefits = listOf("Calms nerves", "Reduces stress", "Improves focus")
                    )
                )
                breathingExerciseDao.insertAll(sampleExercises)
            }

            suspend fun populateInitialReminders(reminderDao: ReminderDao) {
                if (reminderDao.getAllReminders().firstOrNull()?.isNotEmpty() == true) {
                    return
                }
                val sampleReminders = listOf(
                    Reminder(
                        title = "Morning Medication",
                        description = "Take 2 pills of Vitamin D",
                        dateTime = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0),
                        type = com.example.respiratoryhealthapp.data.model.ReminderType.MEDICATION,
                        recurrence = com.example.respiratoryhealthapp.data.model.ReminderRecurrence.DAILY,
                        isActive = true
                    ),
                    Reminder(
                        title = "Evening Walk",
                        dateTime = LocalDateTime.now().plusDays(1).withHour(18).withMinute(30).withSecond(0),
                        type = com.example.respiratoryhealthapp.data.model.ReminderType.EXERCISE,
                        recurrence = com.example.respiratoryhealthapp.data.model.ReminderRecurrence.NONE,
                        isActive = true
                    )
                )
                reminderDao.insertAll(sampleReminders)
            }

            suspend fun scheduleInitialActiveReminders(reminderDao: ReminderDao, context: Context) {
                val activeReminders = reminderDao.getActiveReminders().firstOrNull()
                if (activeReminders.isNullOrEmpty()) {
                    android.util.Log.d("AppDatabaseCallback", "No initial active reminders to schedule.")
                    return
                }
                val userProfileRepository = UserProfileRepository(context)
                val scheduler = AndroidAlarmScheduler(context, userProfileRepository)
                activeReminders.forEach { reminder ->
                    if (reminder.isActive) {
                        scheduler.schedule(reminder)
                        android.util.Log.d("AppDatabaseCallback", "Scheduled initial reminder: ${reminder.id} - ${reminder.title}")
                    }
                }
            }
        }
    }
} 