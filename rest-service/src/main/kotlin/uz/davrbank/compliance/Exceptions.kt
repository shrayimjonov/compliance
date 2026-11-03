package uz.davrbank.compliance

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.io.OutputStream

class CustomAuthenticationEntryPoint : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        authException: AuthenticationException?
    ) {
        val re =
            BaseMessage(HttpStatus.UNAUTHORIZED.value().toLong(), authException?.message ?: "Authentication failed")
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        val responseStream: OutputStream = response.outputStream
        val mapper = ObjectMapper()
        mapper.writeValue(responseStream, re)
        responseStream.flush()
    }
}

@ControllerAdvice
class ExceptionHandler(
    private val errorMessageSource: ResourceBundleMessageSource,
    private val apiLogRepository: ApiLogRepository
) {

    @ExceptionHandler(Exception::class)
    fun handleThrowable(e: Exception): ResponseEntity<BaseMessage> {
        val logger = KotlinLogging.logger {}
        return when (e) {
            is ComplianceException -> {
                ResponseEntity.badRequest().body(e.getErrorMessage(errorMessageSource))
            }

            is AuthorizationDeniedException -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(BaseMessage(HttpStatus.FORBIDDEN.value().toLong(), e.message))

            else -> {
                logger.info{"error======================>>>>>>>>>>>>>>>>>>>>>>: ${e.message} <<<<<<<<<<<<<<<<<<<<==============================="}
                e.printStackTrace()
                ResponseEntity.badRequest().body(BaseMessage(400, e.message))
            }
        }
    }
}


sealed class ComplianceException(msg: String? = null) : RuntimeException(msg) {

    abstract fun errorCode(): ErrorCode

    fun response(source: MessageSource, vararg args: Any) =
        BaseMessage(errorCode().code, source.getMessage(errorCode().name, args, LocaleContextHolder.getLocale()))

    open fun getErrorMessageArguments(): Array<Any?>? = null

    fun getErrorMessage(errorMessageSource: ResourceBundleMessageSource): BaseMessage {
        val errorMessage = try {
            errorMessageSource.getMessage(
                errorCode().name,
                getErrorMessageArguments(),
                LocaleContextHolder.getLocale()
            )
        } catch (e: Exception) {
            e.message
        }
        return BaseMessage(errorCode().code, errorMessage)
    }
}

class GeneralApiException(private val msg: String) : ComplianceException(msg) {
    override fun errorCode() = ErrorCode.GENERAL_API_EXCEPTION

    override fun getErrorMessageArguments(): Array<Any?> = arrayOf(msg)
}

class UserNotFoundException() : ComplianceException() {
    override fun errorCode() = ErrorCode.USER_NOT_FOUND_EXCEPTION

    override fun getErrorMessageArguments(): Array<Any?> = arrayOf()
}

class UnsupportedGrantTypeException(private val grantType: String) : ComplianceException(grantType) {
    override fun errorCode() = ErrorCode.UNSUPPORTED_GRANT_TYPE

    override fun getErrorMessageArguments(): Array<Any?> = arrayOf(grantType)
}

class ValidationException(message: String) : ComplianceException(message) {
    override fun errorCode(): ErrorCode = ErrorCode.GENERAL_API_EXCEPTION
    override fun getErrorMessageArguments(): Array<Any?> = arrayOf(message)
}