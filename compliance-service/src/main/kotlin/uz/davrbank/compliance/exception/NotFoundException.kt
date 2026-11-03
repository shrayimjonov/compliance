package uz.davrbank.compliance.exception

import org.springframework.http.HttpStatus

class NotFoundException(
    message: String,
    details: List<String> = emptyList(),
    code: String = "NOT_FOUND",
) : ApiException(
    status = HttpStatus.NOT_FOUND,
    message = message,
    code = code,
    details = details,
)
