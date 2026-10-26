package io.github.pwrmind.radioexam

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyTestsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DatabaseProvider.getDatabase(application)
    private val testDao = database.testDao()

    private val _inProgressTests = MutableStateFlow<List<AttemptSummary>>(emptyList())
    val inProgressTests: StateFlow<List<AttemptSummary>> = _inProgressTests.asStateFlow()

    private val _completedTests = MutableStateFlow<List<AttemptSummary>>(emptyList())
    val completedTests: StateFlow<List<AttemptSummary>> = _completedTests.asStateFlow()

    init {
        loadTests()
    }

    fun loadTests() {
        viewModelScope.launch {
            _inProgressTests.value = testDao.getInProgressSummaries()
            _completedTests.value = testDao.getCompletedSummaries()
        }
    }
}