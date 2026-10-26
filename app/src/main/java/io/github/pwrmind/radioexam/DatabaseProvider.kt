package io.github.pwrmind.radioexam

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

object DatabaseProvider {
    @Volatile
    private var INSTANCE: io.github.pwrmind.radioexam.AppDatabase? = null

    fun getDatabase(context: Context): io.github.pwrmind.radioexam.AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                _root_ide_package_.io.github.pwrmind.radioexam.AppDatabase::class.java,
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