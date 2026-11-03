package uz.davrbank.compliance.dto.request

import jakarta.validation.constraints.NotBlank

data class EntityTranslationRequest(
    val entityId: String? = null,
    @field:NotBlank
    val language: String,
    val originalText: String? = null,
)
