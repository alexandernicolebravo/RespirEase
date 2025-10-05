package com.example.respiratoryhealthapp.data.repository

import com.example.respiratoryhealthapp.data.model.ContentCategory
import com.example.respiratoryhealthapp.data.model.ContentDifficulty
import com.example.respiratoryhealthapp.data.model.EducationalArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class LocalEducationalContentRepository : EducationalContentRepository {
    private val articles = MutableStateFlow<List<EducationalArticle>>(sampleArticles)

    override fun getAllArticles(): Flow<List<EducationalArticle>> = articles

    override fun getArticlesByCategory(category: ContentCategory): Flow<List<EducationalArticle>> =
        articles.map { it.filter { article -> article.category == category } }

    override fun getArticlesByDifficulty(difficulty: ContentDifficulty): Flow<List<EducationalArticle>> =
        articles.map { it.filter { article -> article.difficulty == difficulty } }

    override fun getBookmarkedArticles(): Flow<List<EducationalArticle>> =
        articles.map { it.filter { article -> article.isBookmarked } }

    override fun searchArticles(query: String): Flow<List<EducationalArticle>> =
        articles.map { articles ->
            articles.filter { article ->
                article.title.contains(query, ignoreCase = true) ||
                article.summary.contains(query, ignoreCase = true) ||
                article.tags.any { it.contains(query, ignoreCase = true) }
            }
        }

    override suspend fun toggleBookmark(articleId: String) {
        articles.value = articles.value.map { article ->
            if (article.id == articleId) {
                article.copy(isBookmarked = !article.isBookmarked)
            } else {
                article
            }
        }
    }

    override suspend fun getArticleById(id: String): EducationalArticle? =
        articles.value.find { it.id == id }

    override suspend fun updateArticle(article: EducationalArticle) {
        articles.value = articles.value.map {
            if (it.id == article.id) article else it
        }
    }

    override suspend fun addArticle(article: EducationalArticle) {
        articles.value = articles.value + article
    }

    override suspend fun deleteArticle(id: String) {
        articles.value = articles.value.filter { it.id != id }
    }

    companion object {
        private val sampleArticles = listOf(
            EducationalArticle(
                id = "1",
                title = "Understanding Asthma: A Comprehensive Guide",
                summary = "Learn about asthma triggers, symptoms, and management strategies.",
                fullContent = "Asthma is a chronic respiratory condition that affects millions worldwide...",
                category = ContentCategory.ASTHMA,
                readingTime = 10,
                difficulty = ContentDifficulty.BEGINNER,
                tags = listOf("asthma", "basics", "management")
            ),
            EducationalArticle(
                id = "2",
                title = "COPD Management: Advanced Techniques",
                summary = "Advanced strategies for managing COPD symptoms and improving quality of life.",
                fullContent = "Chronic Obstructive Pulmonary Disease (COPD) requires careful management...",
                category = ContentCategory.COPD,
                readingTime = 15,
                difficulty = ContentDifficulty.ADVANCED,
                tags = listOf("COPD", "advanced", "management")
            ),
            EducationalArticle(
                id = "3",
                title = "Emergency Response Guide",
                summary = "What to do during respiratory emergencies and when to seek help.",
                fullContent = "During a respiratory emergency, quick and appropriate action is crucial...",
                category = ContentCategory.EMERGENCY_GUIDE,
                readingTime = 5,
                difficulty = ContentDifficulty.BEGINNER,
                tags = listOf("emergency", "safety", "first-aid")
            ),
            EducationalArticle(
                id = "4",
                title = "Medication Guide: Inhalers and Their Use",
                summary = "A detailed guide to different types of inhalers and proper usage techniques.",
                fullContent = "Inhalers are essential tools in managing respiratory conditions...",
                category = ContentCategory.MEDICATION_GUIDE,
                readingTime = 12,
                difficulty = ContentDifficulty.INTERMEDIATE,
                tags = listOf("medication", "inhalers", "techniques")
            ),
            EducationalArticle(
                id = "5",
                title = "Lifestyle Tips for Better Breathing",
                summary = "Practical tips for improving respiratory health through lifestyle changes.",
                fullContent = "Making certain lifestyle changes can significantly improve your respiratory health...",
                category = ContentCategory.LIFESTYLE_TIPS,
                readingTime = 8,
                difficulty = ContentDifficulty.BEGINNER,
                tags = listOf("lifestyle", "wellness", "tips")
            )
        )
    }
} 