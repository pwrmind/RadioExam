package io.github.pwrmind.radioexam

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TheoryItem(
    val question: QuestionEntity,
    val correctAnswer: AnswerEntity?
)

class TheoryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DatabaseProvider.getDatabase(application)
    private val testDao = database.testDao()

    private val _sectionName = MutableStateFlow("")
    val sectionName: StateFlow<String> = _sectionName.asStateFlow()

    private val _items = MutableStateFlow<List<TheoryItem>>(emptyList())
    val items: StateFlow<List<TheoryItem>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun load(sectionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val section = testDao.getSectionById(sectionId)
                _sectionName.value = section?.name ?: sectionId

                // Для экрана теории собираем все вопросы, которые могут
                // встретиться в этом экзамене (по темам из правил).
                val rules = testDao.getExamRules(sectionId)
                val allQuestions = mutableListOf<QuestionEntity>()
                for (rule in rules) {
                    allQuestions += testDao.getAllQuestionsByTopic(rule.topicId)
                }

                val result = allQuestions.map { q ->
                    val answers = testDao.getAnswers(q.id)
                    TheoryItem(
                        question = q,
                        correctAnswer = answers.firstOrNull { it.isCorrect }
                    )
                }
                _items.value = result
            } catch (e: Exception) {
                _items.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}