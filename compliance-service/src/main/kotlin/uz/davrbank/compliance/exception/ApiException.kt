package uz.davrbank.compliance.exception

import org.springframework.http.HttpStatus

open class ApiException(
    val status: HttpStatus,
    override val message: String,
    val code: Any? = null,
    val details: List<String> = emptyList(),
    cause: Throwable? = null,
) : RuntimeException(message, cause)
