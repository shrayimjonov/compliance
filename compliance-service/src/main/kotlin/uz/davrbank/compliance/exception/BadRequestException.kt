package uz.davrbank.compliance.exception

import org.springframework.http.HttpStatus

class BadRequestException(
    message: String,
    details: List<String> = emptyList(),
    code: String = "BAD_REQUEST",
) : ApiException(
    status = HttpStatus.BAD_REQUEST,
    message = message,
    code = code,
    details = details,
)
