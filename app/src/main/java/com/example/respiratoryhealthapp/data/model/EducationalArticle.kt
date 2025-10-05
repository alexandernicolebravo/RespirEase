package com.example.respiratoryhealthapp.data.model

enum class ContentCategory {
    ASTHMA,
    COPD,
    ALLERGIC_RHINITIS,
    GENERAL_WELLNESS,
    MEDICATION_GUIDE,
    LIFESTYLE_TIPS,
    EMERGENCY_GUIDE,
    TREATMENT_OPTIONS;

    val displayName: String
        get() = when (this) {
            ASTHMA -> "Asthma"
            COPD -> "COPD"
            ALLERGIC_RHINITIS -> "Allergic Rhinitis"
            GENERAL_WELLNESS -> "General Wellness"
            MEDICATION_GUIDE -> "Medication Guide"
            LIFESTYLE_TIPS -> "Lifestyle Tips"
            EMERGENCY_GUIDE -> "Emergency Guide"
            TREATMENT_OPTIONS -> "Treatment Options"
        }
}

data class EducationalArticle(
    val id: String,
    val title: String,
    val summary: String,
    val fullContent: String,
    val category: ContentCategory,
    val imageUrl: String? = null,
    val readingTime: Int, // Estimated reading time in minutes
    val difficulty: ContentDifficulty = ContentDifficulty.INTERMEDIATE,
    val tags: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false
)

enum class ContentDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED;

    val displayName: String
        get() = when (this) {
            BEGINNER -> "Beginner"
            INTERMEDIATE -> "Intermediate"
            ADVANCED -> "Advanced"
        }
} 