package com.example.respiratoryhealthapp.screens.educationalcontent

import com.example.respiratoryhealthapp.data.model.ContentCategory
import com.example.respiratoryhealthapp.data.model.ContentDifficulty
import com.example.respiratoryhealthapp.data.model.EducationalArticle

data class EducationalContentUiState(
    val articles: List<EducationalArticle> = emptyList(),
    val selectedCategory: ContentCategory? = null,
    val selectedDifficulty: ContentDifficulty? = null,
    val searchQuery: String = "",
    val showBookmarkedOnly: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) 