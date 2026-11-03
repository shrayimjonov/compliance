package uz.davrbank.compliance.exception

import org.springframework.http.HttpStatus

class ServiceUnavailableException(
    message: String,
    code: String = "SERVICE_UNAVAILABLE",
    details: List<String> = emptyList(),
    cause: Throwable? = null,
) : ApiException(
    status = HttpStatus.SERVICE_UNAVAILABLE,
    message = message,
    code = code,
    details = details,
    cause = cause,
)
