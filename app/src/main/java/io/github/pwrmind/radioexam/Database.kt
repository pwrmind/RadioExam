package io.example.radioexam

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update

// --- ТАБЛИЦЫ КОНТЕНТА ---
@Entity(tableName = "sections")
data class SectionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val pickCount: Int
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: Int,
    val topicId: String,   // "4-1", "3-2", ... — тема, к которой относится вопрос
    val text: String,
    val hint: String?
)

@Entity(tableName = "answers")
data class AnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: Int,
    val text: String,
    val isCorrect: Boolean
)

// --- ПРАВИЛА ЭКЗАМЕНОВ (КОНФИГУРАЦИЯ) ---
@Entity(tableName = "exam_rules")
data class ExamRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sectionId: String,   // "1", "2", "3", "4"
    val topicId: String,     // "4-1", "2-5", ...
    val pickCount: Int       // сколько вопросов взять из этой темы
)

// --- ТАБЛИЦЫ ПРОГРЕССА ---
@Entity(tableName = "attempts")
data class AttemptEntity(
    @PrimaryKey(autoGenerate = true) val attemptId: Long = 0,
    val sectionId: String,
    val isCompleted: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis(),
    val secondsLeft: Int = -1,
    val totalQuestions: Int = 0
)

@Entity(tableName = "user_answers", primaryKeys = ["attemptId", "questionId"])
data class UserAnswerEntity(
    val attemptId: Long,
    val questionId: Int,
    val selectedAnswerId: Long?,
    val status: String
)

// --- СВОДКИ ---
data class AttemptSummary(
    val attemptId: Long,
    val sectionId: String,
    val sectionName: String,
    val lastUpdated: Long,
    val totalCount: Int,
    val answeredCount: Int,
    val correctCount: Int
)

data class SectionWithProgress(
    val id: String,
    val name: String,
    val pickCount: Int,
    val activeAttemptId: Long?,
    val totalInAttempt: Int,
    val answeredCount: Int
)

// --- DAO ---
@Dao
interface TestDao {
    @Query("SELECT * FROM sections")
    suspend fun getAllSections(): List<io.example.radioexam.SectionEntity>

    @Query("SELECT * FROM sections WHERE id = :sectionId")
    suspend fun getSectionById(sectionId: String): io.example.radioexam.SectionEntity?

    // --- Правила и выборка ---
    @Query("SELECT * FROM exam_rules WHERE sectionId = :sectionId")
    suspend fun getExamRules(sectionId: String): List<io.example.radioexam.ExamRuleEntity>

    @Query("SELECT * FROM questions WHERE topicId = :topicId ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomQuestionsByTopic(topicId: String, count: Int): List<io.example.radioexam.QuestionEntity>

    // --- Для экрана "Теория" ---
    @Query("SELECT * FROM questions WHERE topicId = :topicId ORDER BY id")
    suspend fun getAllQuestionsByTopic(topicId: String): List<io.example.radioexam.QuestionEntity>

    @Query("SELECT * FROM answers WHERE questionId = :questionId")
    suspend fun getAnswers(questionId: Int): List<io.example.radioexam.AnswerEntity>

    // --- Попытки ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: io.example.radioexam.AttemptEntity): Long

    @Query("SELECT * FROM attempts WHERE sectionId = :sectionId AND isCompleted = 0 ORDER BY lastUpdated DESC LIMIT 1")
    suspend fun getActiveAttempt(sectionId: String): io.example.radioexam.AttemptEntity?

    @Update
    suspend fun updateAttempt(attempt: io.example.radioexam.AttemptEntity)

    @Query(
        "UPDATE attempts SET secondsLeft = :secondsLeft, lastUpdated = :lastUpdated " +
                "WHERE attemptId = :attemptId AND isCompleted = 0"
    )
    suspend fun updateTimer(attemptId: Long, secondsLeft: Int, lastUpdated: Long)

    @Query(
        "UPDATE attempts SET isCompleted = 1, lastUpdated = :lastUpdated " +
                "WHERE attemptId = :attemptId"
    )
    suspend fun finishAttempt(attemptId: Long, lastUpdated: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAnswer(answer: io.example.radioexam.UserAnswerEntity)

    @Query("SELECT * FROM user_answers WHERE attemptId = :attemptId")
    suspend fun getUserAnswersForAttempt(attemptId: Long): List<io.example.radioexam.UserAnswerEntity>

    @Query("SELECT * FROM questions WHERE id IN (SELECT questionId FROM user_answers WHERE attemptId = :attemptId)")
    suspend fun getQuestionsForAttempt(attemptId: Long): List<io.example.radioexam.QuestionEntity>

    // --- Сводки ---
    @Query(
        """
        SELECT
            a.attemptId AS attemptId,
            a.sectionId AS sectionId,
            s.name AS sectionName,
            a.lastUpdated AS lastUpdated,
            a.totalQuestions AS totalCount,
            COUNT(CASE WHEN ua.status != 'UNTOUCHED' THEN 1 END) AS answeredCount,
            COALESCE(SUM(CASE WHEN ua.status = 'CORRECT' THEN 1 ELSE 0 END), 0) AS correctCount
        FROM attempts a
        INNER JOIN sections s ON s.id = a.sectionId
        LEFT JOIN user_answers ua ON ua.attemptId = a.attemptId
        WHERE a.isCompleted = 0
        GROUP BY a.attemptId
        ORDER BY a.lastUpdated DESC
        """
    )
    suspend fun getInProgressSummaries(): List<io.example.radioexam.AttemptSummary>

    @Query(
        """
        SELECT
            a.attemptId AS attemptId,
            a.sectionId AS sectionId,
            s.name AS sectionName,
            a.lastUpdated AS lastUpdated,
            a.totalQuestions AS totalCount,
            COUNT(CASE WHEN ua.status != 'UNTOUCHED' THEN 1 END) AS answeredCount,
            COALESCE(SUM(CASE WHEN ua.status = 'CORRECT' THEN 1 ELSE 0 END), 0) AS correctCount
        FROM attempts a
        INNER JOIN sections s ON s.id = a.sectionId
        LEFT JOIN user_answers ua ON ua.attemptId = a.attemptId
        WHERE a.isCompleted = 1
        GROUP BY a.attemptId
        ORDER BY a.lastUpdated DESC
        """
    )
    suspend fun getCompletedSummaries(): List<io.example.radioexam.AttemptSummary>

    @Query(
        """
        SELECT
            s.id AS id,
            s.name AS name,
            s.pickCount AS pickCount,
            a.attemptId AS activeAttemptId,
            COALESCE(a.totalQuestions, 0) AS totalInAttempt,
            COALESCE(
                (SELECT COUNT(*) FROM user_answers WHERE attemptId = a.attemptId AND status != 'UNTOUCHED'),
                0
            ) AS answeredCount
        FROM sections s
        LEFT JOIN attempts a ON a.sectionId = s.id AND a.isCompleted = 0
        ORDER BY s.id
        """
    )
    suspend fun getSectionsWithProgress(): List<io.example.radioexam.SectionWithProgress>
}

@Database(
    entities = [
        _root_ide_package_.io.example.radioexam.SectionEntity::class,
        _root_ide_package_.io.example.radioexam.QuestionEntity::class,
        _root_ide_package_.io.example.radioexam.AnswerEntity::class,
        _root_ide_package_.io.example.radioexam.ExamRuleEntity::class,
        _root_ide_package_.io.example.radioexam.AttemptEntity::class,
        _root_ide_package_.io.example.radioexam.UserAnswerEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun testDao(): io.example.radioexam.TestDao
}