package com.example.respiratoryhealthapp.screens.educationalcontent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.respiratoryhealthapp.data.model.ContentCategory
import com.example.respiratoryhealthapp.data.model.ContentDifficulty
import com.example.respiratoryhealthapp.data.model.EducationalArticle
import com.example.respiratoryhealthapp.data.repository.EducationalContentRepository
import com.example.respiratoryhealthapp.data.repository.LocalEducationalContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EducationalContentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EducationalContentRepository = LocalEducationalContentRepository()
    private val _uiState = MutableStateFlow(EducationalContentUiState())
    val uiState: StateFlow<EducationalContentUiState> = _uiState.asStateFlow()

    init {
        loadArticles()
    }

    private fun loadArticles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.getAllArticles().collect { articles ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            articles = filterArticles(articles),
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
    }
        }
    }

    private fun filterArticles(articles: List<EducationalArticle>): List<EducationalArticle> {
        return articles.filter { article ->
            val matchesCategory = _uiState.value.selectedCategory == null || 
                                article.category == _uiState.value.selectedCategory
            val matchesDifficulty = _uiState.value.selectedDifficulty == null || 
                                  article.difficulty == _uiState.value.selectedDifficulty
            val matchesSearch = _uiState.value.searchQuery.isEmpty() ||
                              article.title.contains(_uiState.value.searchQuery, ignoreCase = true) ||
                              article.summary.contains(_uiState.value.searchQuery, ignoreCase = true) ||
                              article.tags.any { it.contains(_uiState.value.searchQuery, ignoreCase = true) }
            val matchesBookmark = !_uiState.value.showBookmarkedOnly || article.isBookmarked

            matchesCategory && matchesDifficulty && matchesSearch && matchesBookmark
        }
    }

    fun setCategory(category: ContentCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadArticles()
    }

    fun setDifficulty(difficulty: ContentDifficulty?) {
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
        loadArticles()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadArticles()
    }

    fun toggleBookmarkedOnly() {
        _uiState.update { it.copy(showBookmarkedOnly = !it.showBookmarkedOnly) }
        loadArticles()
    }

    fun toggleBookmark(articleId: String) {
        viewModelScope.launch {
            try {
                repository.toggleBookmark(articleId)
                loadArticles()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun getArticleById(id: String): EducationalArticle? {
        return _uiState.value.articles.find { it.id == id }
    }
} 