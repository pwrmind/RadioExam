package com.example.radioexam

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class SeedDatabaseCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        Log.d("SeedDatabaseCallback", "onCreate: База данных создается. Начинаем заполнение...")

        db.execSQL("INSERT INTO sections (id, name, pickCount) VALUES ('4-1', '4-1 (РР МСЭ, СЕПТ)', 3)")
        db.execSQL("INSERT INTO sections (id, name, pickCount) VALUES ('4-2', '4-2 (Спектр, РФ)', 3)")
        db.execSQL("INSERT INTO sections (id, name, pickCount) VALUES ('2-5', '2-5 (РП, РПУ, ант., распр. радиоволн)', 5)")

        db.execSQL("INSERT INTO questions (id, sectionId, text, hint) VALUES (150, '4-1', 'В каком случае рекомендуется включать малошумящий предварительный усилитель (PREAMP), установленный на входе приёмника радиостанции?', 'Подумайте, что происходит с шумами при усилении слабого сигнала.')")
        db.execSQL("INSERT INTO questions (id, sectionId, text, hint) VALUES (151, '4-1', 'В каком случае рекомендуется включать аттенюатор (АТТ)?', 'Аттенюатор — это ослабитель сигнала на входе.')")
        db.execSQL("INSERT INTO questions (id, sectionId, text, hint) VALUES (57, '4-2', 'На каких диапазонах разрешено осуществлять передачи радиооператорам любительских радиостанций четвертой категории самостоятельно с собственной радиостанцией?', 'Вспомните самые короткие волны.')")

        db.execSQL("INSERT INTO answers (questionId, text, isCorrect) VALUES (150, 'При приёме слабых сигналов', 1)")
        db.execSQL("INSERT INTO answers (questionId, text, isCorrect) VALUES (150, 'При приёме сильных сигналов', 0)")
        db.execSQL("INSERT INTO answers (questionId, text, isCorrect) VALUES (150, 'При высоком уровне внешнего шума', 0)")
        db.execSQL("INSERT INTO answers (questionId, text, isCorrect) VALUES (150, 'При недостаточной громкости сигнала в головных телефонах', 0)")

        db.execSQL("INSERT INTO answers (questionId, text, isCorrect) VALUES (151, 'При приёме сильных сигналов', 1)")
        db.execSQL("INSERT INTO answers (questionId, text, isCorrect) VALUES (151, 'При приёме слабых сигналов', 0)")
        db.execSQL("INSERT INTO answers (questionId, text, isCorrect) VALUES (151, 'При недостаточной мощности выходного каскада', 0)")
        db.execSQL("INSERT INTO answers (questionId, text, isCorrect) VALUES (151, 'При недостаточной мощности усилителя звуковой частоты', 0)")

        db.execSQL("INSERT INTO answers (questionId, text, isCorrect) VALUES (57, 'Только на УКВ - диапазонах', 1)")
        db.execSQL("INSERT INTO answers (questionId, text, isCorrect) VALUES (57, 'На диапазоне 160 метров и УКВ-диапазонах', 0)")
        db.execSQL("INSERT INTO answers (questionId, text, isCorrect) VALUES (57, 'Только на диапазоне 160 метров', 0)")
        db.execSQL("INSERT INTO answers (questionId, text, isCorrect) VALUES (57, 'На всех диапазонах, выделенных любительской службе в России', 0)")

        Log.d("SeedDatabaseCallback", "База данных успешно заполнена начальными данными.")
    }
}

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "radioexam_v6.db"
            )
                .fallbackToDestructiveMigration()
                .addCallback(SeedDatabaseCallback())
                .build()
            INSTANCE = instance
            instance
        }
    }
}