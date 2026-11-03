package uz.davrbank.compliance.dto.response

import uz.davrbank.compliance.enums.Language

data class EntityTranslationResponse(
    val originalText: String,
    val translatedText: String,
    val language: Language,
    val latency: Long,
)