package io.github.pwrmind.radioexam

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CatalogViewModel(application: Application) : AndroidViewModel(application) {

    private val database = _root_ide_package_.io.github.pwrmind.radioexam.DatabaseProvider.getDatabase(application)
    private val testDao = database.testDao()

    private val _sections = MutableStateFlow<List<io.github.pwrmind.radioexam.SectionWithProgress>>(emptyList())
    val sections: StateFlow<List<io.github.pwrmind.radioexam.SectionWithProgress>> = _sections.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadSections()
    }

    fun loadSections() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = testDao.getSectionsWithProgress()
                Log.d("CatalogViewModel", "Загружено секций: ${list.size}")
                _sections.value = list
            } catch (e: Exception) {
                Log.e("CatalogViewModel", "Ошибка загрузки секций из БД", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}