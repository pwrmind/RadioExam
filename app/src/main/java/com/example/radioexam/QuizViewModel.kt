package com.example.radioexam

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class QuestionStatus {
    UNTOUCHED, SKIPPED, CORRECT, INCORRECT
}

data class UserAnswerState(
    val status: QuestionStatus,
    val selectedAnswerId: Long? = null
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DatabaseProvider.getDatabase(application)
    private val testDao = database.testDao()

    private val _questions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val questions: StateFlow<List<QuestionEntity>> = _questions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _currentAnswers = MutableStateFlow<List<AnswerEntity>>(emptyList())
    val currentAnswers: StateFlow<List<AnswerEntity>> = _currentAnswers.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _userAnswers = MutableStateFlow<Map<Int, UserAnswerState>>(emptyMap())
    val userAnswers: StateFlow<Map<Int, UserAnswerState>> = _userAnswers.asStateFlow()

    private val _currentSelectedAnswerId = MutableStateFlow<Long?>(null)
    val currentSelectedAnswerId: StateFlow<Long?> = _currentSelectedAnswerId.asStateFlow()

    private val _formattedTime = MutableStateFlow("--:--")
    val formattedTime: StateFlow<String> = _formattedTime.asStateFlow()

    private val _timeExpired = MutableStateFlow(false)
    val timeExpired: StateFlow<Boolean> = _timeExpired.asStateFlow()

    private var currentAttemptId: Long = 0L
    private var testEndTimeMillis: Long = 0L
    private var timerJob: Job? = null
    private var isFinishing: Boolean = false
    private var toneGenerator: ToneGenerator? = null

    fun loadTest(sectionId: String) {
        isFinishing = false
        _timeExpired.value = false

        viewModelScope.launch {
            val existingAttempt = testDao.getActiveAttempt(sectionId)

            if (existingAttempt != null) {
                currentAttemptId = existingAttempt.attemptId

                val loadedQuestions = testDao.getQuestionsForAttempt(currentAttemptId)
                _questions.value = loadedQuestions

                if (existingAttempt.totalQuestions <= 0 && loadedQuestions.isNotEmpty()) {
                    testDao.updateAttempt(
                        existingAttempt.copy(totalQuestions = loadedQuestions.size)
                    )
                }

                val savedAnswers = testDao.getUserAnswersForAttempt(currentAttemptId)
                val newMap = mutableMapOf<Int, UserAnswerState>()
                var calculatedScore = 0

                loadedQuestions.forEachIndexed { index, question ->
                    val saved = savedAnswers.find { it.questionId == question.id }
                    if (saved != null) {
                        val status = QuestionStatus.valueOf(saved.status)
                        newMap[index] = UserAnswerState(status, saved.selectedAnswerId)
                        if (status == QuestionStatus.CORRECT) calculatedScore++
                    }
                }
                _userAnswers.value = newMap
                _score.value = calculatedScore
                _currentQuestionIndex.value = 0

                val maxSeconds = loadedQuestions.size * 60
                val secondsLeft = if (existingAttempt.secondsLeft >= 0) {
                    existingAttempt.secondsLeft
                } else {
                    maxSeconds
                }
                testEndTimeMillis = existingAttempt.lastUpdated + secondsLeft * 1000L

                val remainingNow =
                    ((testEndTimeMillis - System.currentTimeMillis()) / 1000L).toInt()
                if (remainingNow <= 0) {
                    _formattedTime.value = "00:00"
                    finishTest()
                    _timeExpired.value = true
                } else {
                    startTimer(remainingNow)
                }
            } else {
                val section = testDao.getSectionById(sectionId)
                val count = section?.pickCount ?: 5
                val loadedQuestions = testDao.getRandomQuestions(sectionId, count)
                val maxSeconds = loadedQuestions.size * 60

                val newAttempt = AttemptEntity(
                    sectionId = sectionId,
                    secondsLeft = maxSeconds,
                    lastUpdated = System.currentTimeMillis(),
                    totalQuestions = loadedQuestions.size
                )
                currentAttemptId = testDao.insertAttempt(newAttempt)

                _questions.value = loadedQuestions
                _currentQuestionIndex.value = 0
                _score.value = 0
                _userAnswers.value = emptyMap()

                testEndTimeMillis = System.currentTimeMillis() + maxSeconds * 1000L
                startTimer(maxSeconds)
            }

            _currentSelectedAnswerId.value = null
            if (_questions.value.isNotEmpty()) {
                loadAnswersForQuestion(_questions.value[0].id)
            }
        }
    }

    private fun startTimer(initialSeconds: Int) {
        timerJob?.cancel()
        updateFormattedTime(initialSeconds)

        timerJob = viewModelScope.launch {
            while (true) {
                val remaining =
                    ((testEndTimeMillis - System.currentTimeMillis()) / 1000L).toInt()
                if (remaining <= 0) {
                    updateFormattedTime(0)
                    finishTest()
                    _timeExpired.value = true
                    break
                }
                updateFormattedTime(remaining)
                delay(1000L)
            }
        }
    }

    private fun updateFormattedTime(totalSeconds: Int) {
        val safe = totalSeconds.coerceAtLeast(0)
        val minutes = safe / 60
        val seconds = safe % 60
        _formattedTime.value = String.format("%02d:%02d", minutes, seconds)
    }

    fun selectAnswer(answerId: Long) {
        _currentSelectedAnswerId.value = answerId
    }

    fun checkAnswer() {
        val selectedId = _currentSelectedAnswerId.value ?: return
        val answers = _currentAnswers.value
        val selectedAnswer = answers.find { it.id == selectedId }
        val isCorrect = selectedAnswer?.isCorrect == true
        val questionId = _questions.value[_currentQuestionIndex.value].id

        playFeedbackSound(isCorrect)

        val newStatus = if (isCorrect) QuestionStatus.CORRECT else QuestionStatus.INCORRECT
        val currentMap = _userAnswers.value.toMutableMap()
        currentMap[_currentQuestionIndex.value] = UserAnswerState(newStatus, selectedId)
        _userAnswers.value = currentMap
        _score.value = currentMap.values.count { it.status == QuestionStatus.CORRECT }

        viewModelScope.launch {
            testDao.insertUserAnswer(
                UserAnswerEntity(
                    attemptId = currentAttemptId,
                    questionId = questionId,
                    selectedAnswerId = selectedId,
                    status = newStatus.name
                )
            )
        }
        saveTimerProgress()
    }

    fun skipQuestion() {
        if (_questions.value.isEmpty()) return
        val questionId = _questions.value[_currentQuestionIndex.value].id
        val currentMap = _userAnswers.value.toMutableMap()
        currentMap[_currentQuestionIndex.value] = UserAnswerState(QuestionStatus.SKIPPED)
        _userAnswers.value = currentMap
        _score.value = currentMap.values.count { it.status == QuestionStatus.CORRECT }

        viewModelScope.launch {
            testDao.insertUserAnswer(
                UserAnswerEntity(
                    attemptId = currentAttemptId,
                    questionId = questionId,
                    selectedAnswerId = null,
                    status = QuestionStatus.SKIPPED.name
                )
            )
        }
        saveTimerProgress()

        goToNextQuestion()
    }

    fun goToNextQuestion() {
        val nextIndex = _currentQuestionIndex.value + 1
        if (nextIndex < _questions.value.size) {
            jumpToQuestion(nextIndex)
        }
    }

    fun jumpToQuestion(index: Int) {
        if (index in _questions.value.indices) {
            _currentQuestionIndex.value = index
            val savedState = _userAnswers.value[index]
            _currentSelectedAnswerId.value = savedState?.selectedAnswerId
            loadAnswersForQuestion(_questions.value[index].id)
        }
    }

    fun finishTest() {
        if (isFinishing) return
        isFinishing = true
        timerJob?.cancel()
        timerJob = null
        _formattedTime.value = "00:00"

        val id = currentAttemptId
        if (id <= 0L) return
        val dao = testDao
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                dao.finishAttempt(id, System.currentTimeMillis())
            } catch (e: Exception) {
                Log.e("QuizViewModel", "Не удалось завершить попытку", e)
            }
        }
    }

    private fun saveTimerProgress() {
        if (currentAttemptId <= 0L || isFinishing) return
        val remaining = ((testEndTimeMillis - System.currentTimeMillis()) / 1000L)
            .toInt().coerceAtLeast(0)
        val id = currentAttemptId
        val dao = testDao
        viewModelScope.launch {
            try {
                dao.updateTimer(id, remaining, System.currentTimeMillis())
            } catch (e: Exception) {
                Log.e("QuizViewModel", "Не удалось сохранить таймер", e)
            }
        }
    }

    private fun loadAnswersForQuestion(questionId: Int) {
        viewModelScope.launch {
            val answers = testDao.getAnswers(questionId)
            _currentAnswers.value = answers.shuffled()
        }
    }

    private fun playFeedbackSound(isCorrect: Boolean) {
        try {
            val generator = toneGenerator ?: ToneGenerator(AudioManager.STREAM_MUSIC, 80).also {
                toneGenerator = it
            }
            if (isCorrect) {
                generator.startTone(ToneGenerator.TONE_PROP_ACK, 150)
            } else {
                generator.startTone(ToneGenerator.TONE_PROP_NACK, 300)
            }
        } catch (e: Exception) {
            Log.e("QuizViewModel", "Не удалось воспроизвести звук", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        timerJob = null

        try {
            toneGenerator?.release()
        } catch (_: Exception) {
        }
        toneGenerator = null

        if (currentAttemptId > 0L && !isFinishing) {
            val remaining = ((testEndTimeMillis - System.currentTimeMillis()) / 1000L)
                .toInt().coerceAtLeast(0)
            val id = currentAttemptId
            val dao = testDao
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    dao.updateTimer(id, remaining, System.currentTimeMillis())
                } catch (_: Exception) {
                }
            }
        }
    }
}