package uz.davrbank.compliance.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorDTO> = build(
        status = HttpStatus.BAD_REQUEST,
        message = "Validation failed",
        request = request,
        details = ex.bindingResult.allErrors.toErrorDetails(),
        code = VALIDATION_ERROR_CODE,
    )

    @ExceptionHandler(BindException::class)
    fun handleBindException(
        ex: BindException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorDTO> = build(
        status = HttpStatus.BAD_REQUEST,
        message = "Validation failed",
        request = request,
        details = ex.bindingResult.allErrors.toErrorDetails(),
        code = VALIDATION_ERROR_CODE,
    )

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        ex: ConstraintViolationException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorDTO> = build(
        status = HttpStatus.BAD_REQUEST,
        message = "Validation failed",
        request = request,
        details = ex.constraintViolations.map { "${it.propertyPath}: ${it.message}" },
        code = VALIDATION_ERROR_CODE,
    )

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleArgumentTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorDTO> = build(
        status = HttpStatus.BAD_REQUEST,
        message = "Validation failed",
        request = request,
        details = listOf("${ex.name}: invalid value '${ex.value}'"),
        code = VALIDATION_ERROR_CODE,
    )

    @ExceptionHandler(ApiException::class)
    fun handleApiException(
        ex: ApiException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorDTO> = build(
        status = ex.status,
        message = ex.message,
        request = request,
        details = ex.details,
        code = ex.code,
    )

    @ExceptionHandler(Exception::class)
    fun handleUnhandledException(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorDTO> {
        logger.error(ex) {
            "Unhandled exception path='${request.requestURI}', method='${request.method}'"
        }

        return build(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            message = "Internal server error",
            request = request,
            details = emptyList(),
            code = INTERNAL_ERROR_CODE,
        )
    }

    private fun build(
        status: HttpStatus,
        message: String,
        request: HttpServletRequest,
        details: List<String>,
        code: Any?,
    ): ResponseEntity<ErrorDTO> {
        val body = ErrorDTO(
            timestamp = LocalDateTime.now(),
            status = status.value(),
            code = code,
            error = status.reasonPhrase,
            message = message,
            path = request.requestURI,
            details = details,
        )

        return ResponseEntity.status(status).body(body)
    }

    private fun List<org.springframework.validation.ObjectError>.toErrorDetails(): List<String> = map { error ->
        when (error) {
            is FieldError -> "${error.field}: ${error.defaultMessage ?: "Validation failed"}"
            else -> error.defaultMessage ?: "Validation failed"
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        private const val VALIDATION_ERROR_CODE = "VALIDATION_FAILED"
        private const val INTERNAL_ERROR_CODE = "INTERNAL_SERVER_ERROR"
    }
}
