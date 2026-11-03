package uz.davrbank.compliance.exception

import java.time.LocalDateTime

data class ErrorDTO(
    val timestamp: LocalDateTime,
    val status: Int,
    val code: Any? = null,
    val error: String,
    val message: String,
    val path: String,
    val details: List<String> = emptyList(),
)
