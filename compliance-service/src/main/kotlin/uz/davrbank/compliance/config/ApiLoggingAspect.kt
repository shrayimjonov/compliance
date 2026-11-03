package uz.davrbank.compliance.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uz.davrbank.compliance.dto.request.filter.BaseFilter
import uz.davrbank.compliance.enums.LogSource
import uz.davrbank.compliance.service.ApiLogEvent
import uz.davrbank.compliance.service.ApiLogService
import java.lang.reflect.Method
import java.util.*

@Aspect
@Component
class ApiLoggingAspect(
    private val apiLogService: ApiLogService,
) {
    @Around("@annotation(uz.davrbank.compliance.annotation.LogApiCall)")
    fun logApiCall(joinPoint: ProceedingJoinPoint): Any? {
        val attributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            ?: return joinPoint.proceed()
        val request = attributes.request
        val response = attributes.response
        val method = joinPoint.resolveMethod()
        val start = System.nanoTime()
        var result: Any? = null
        var thrown: Throwable? = null

        try {
            result = joinPoint.proceed()
            return result
        } catch (ex: Throwable) {
            thrown = ex
            throw ex
        } finally {
            val elapsedMs = (System.nanoTime() - start) / NANOS_IN_MILLISECOND
            saveLog(
                joinPoint = joinPoint,
                method = method,
                request = request,
                response = response,
                result = result,
                thrown = thrown,
                elapsedMs = elapsedMs
            )
        }
    }

    private fun saveLog(
        joinPoint: ProceedingJoinPoint,
        method: Method?,
        request: HttpServletRequest,
        response: HttpServletResponse?,
        result: Any?,
        thrown: Throwable?,
        elapsedMs: Long,
    ) {
        runCatching {
            val requestBody = joinPoint.requestBodyArgument(method)
            val username = request.resolveUsername()
            apiLogService.save(
                ApiLogEvent(
                    requestId = resolveRequestId(requestBody),
                    username = username,
                    method = request.method,
                    endpoint = request.requestURI,
                    queryParams = request.queryString,
                    requestBody = requestBody,
                    responseBody = if (thrown == null) result else null,
                    source = username.resolveSource(),
                    statusCode = resolveStatusCode(result, response, thrown),
                    timeTakenMs = elapsedMs,
                )
            )
        }
    }

    private fun resolveRequestId(requestBody: Any?): String =
        requestBody.extractRequestId()
            ?: UUID.randomUUID().toString()

    private fun Any?.extractRequestId(): String? =
        (this as? BaseFilter<*>)?.requestId
            ?.trim()
            ?.takeIf(String::isNotBlank)

    private fun HttpServletRequest.resolveUsername(): String? {
        val headerNames = headerNames.toList()
        val username = headerNames.firstOrNull { headerName ->
            headerName.equals(USERNAME_HEADER_KEYWORD, ignoreCase = true)
        }

        return username
            ?.let(::getHeader)
            ?.trim()
            ?.takeIf(String::isNotBlank)
    }

    private fun String?.resolveSource(): LogSource =
        if (this?.startsWith(NCI_USERNAME_PREFIX, ignoreCase = true) == true) {
            LogSource.NCI
        } else {
            LogSource.SMARTBAS
        }

    private fun ProceedingJoinPoint.requestBodyArgument(method: Method?): Any? {
        if (method == null) {
            return null
        }

        val requestBodyIndex = method.parameterAnnotations.indexOfFirst { annotations ->
            annotations.any { annotation -> annotation.annotationClass.java == RequestBody::class.java }
        }

        return args.getOrNull(requestBodyIndex)
    }

    private fun resolveStatusCode(
        result: Any?,
        response: HttpServletResponse?,
        thrown: Throwable?,
    ): Int =
        when {
            result is ResponseEntity<*> -> result.statusCode.value()
            thrown != null -> response?.status
                ?.takeUnless { status -> status == HttpServletResponse.SC_OK }
                ?: HttpServletResponse.SC_INTERNAL_SERVER_ERROR

            else -> response?.status ?: HttpServletResponse.SC_OK
        }

    private fun ProceedingJoinPoint.resolveMethod(): Method? {
        val method = (signature as? MethodSignature)?.method ?: return null

        return runCatching {
            target.javaClass.getMethod(method.name, *method.parameterTypes)
        }.getOrDefault(method)
    }

    companion object {
        private const val NANOS_IN_MILLISECOND = 1_000_000
        private const val USERNAME_HEADER_KEYWORD = "username"
        private const val NCI_USERNAME_PREFIX = "nci"
    }
}
