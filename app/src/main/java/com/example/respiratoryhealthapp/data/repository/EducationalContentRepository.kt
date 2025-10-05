package com.example.respiratoryhealthapp.data.repository

import com.example.respiratoryhealthapp.data.model.ContentCategory
import com.example.respiratoryhealthapp.data.model.ContentDifficulty
import com.example.respiratoryhealthapp.data.model.EducationalArticle
import kotlinx.coroutines.flow.Flow
 
interface EducationalContentRepository {
    fun getAllArticles(): Flow<List<EducationalArticle>>
    fun getArticlesByCategory(category: ContentCategory): Flow<List<EducationalArticle>>
    fun getArticlesByDifficulty(difficulty: ContentDifficulty): Flow<List<EducationalArticle>>
    fun getBookmarkedArticles(): Flow<List<EducationalArticle>>
    fun searchArticles(query: String): Flow<List<EducationalArticle>>
    suspend fun toggleBookmark(articleId: String)
    suspend fun getArticleById(id: String): EducationalArticle?
    suspend fun updateArticle(article: EducationalArticle)
    suspend fun addArticle(article: EducationalArticle)
    suspend fun deleteArticle(id: String)
} 