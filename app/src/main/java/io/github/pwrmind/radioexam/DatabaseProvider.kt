package io.example.radioexam

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

object DatabaseProvider {
    @Volatile
    private var INSTANCE: io.example.radioexam.AppDatabase? = null

    fun getDatabase(context: Context): io.example.radioexam.AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                _root_ide_package_.io.example.radioexam.AppDatabase::class.java,
                "radioexam_v10.db"
            )
                .createFromAsset("database/app.db")
                .fallbackToDestructiveMigration()
                .build()
            INSTANCE = instance
            instance
        }
    }
}