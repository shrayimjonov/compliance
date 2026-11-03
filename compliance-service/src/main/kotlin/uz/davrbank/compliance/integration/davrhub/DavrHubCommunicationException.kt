package uz.davrbank.compliance.integration.davrhub

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import uz.davrbank.compliance.exception.ApiException

class DavrHubCommunicationException(
    message: String,
    val downstreamStatusCode: HttpStatusCode? = null,
    val responseBody: String? = null,
    cause: Throwable? = null,
) : ApiException(
    status = downstreamStatusCode.toHttpStatusOrServiceUnavailable(),
    message = responseBody.extractDavrHubErrorMessage() ?: message,
    code = responseBody.extractDavrHubErrorCode() ?: DAVRHUB_UNAVAILABLE_CODE,
    details = if (responseBody.hasDavrHubErrorPayload()) {
        emptyList()
    } else {
        downstreamStatusCode?.let { listOf("downstreamStatus=${it.value()}") } ?: emptyList()
    },
    cause = cause,
) {
    companion object {
        private const val DAVRHUB_UNAVAILABLE_CODE = "DAVRHUB_UNAVAILABLE"
        private const val CODE_FIELD = "code"
        private const val MESSAGE_FIELD = "message"

        private val objectMapper = ObjectMapper()

        private fun HttpStatusCode?.toHttpStatusOrServiceUnavailable(): HttpStatus =
            this?.let { HttpStatus.resolve(it.value()) } ?: HttpStatus.SERVICE_UNAVAILABLE

        private fun String?.extractDavrHubErrorCode(): Any? =
            extractDavrHubField(CODE_FIELD)?.toErrorCode()

        private fun String?.extractDavrHubErrorMessage(): String? =
            extractDavrHubField(MESSAGE_FIELD)
                ?.asText()
                ?.trim()
                ?.takeIf(String::isNotBlank)

        private fun String?.hasDavrHubErrorPayload(): Boolean =
            extractDavrHubErrorCode() != null || extractDavrHubErrorMessage() != null

        private fun String?.extractDavrHubField(fieldName: String): JsonNode? {
            val rawBody = this?.trim()?.takeIf(String::isNotBlank) ?: return null

            return runCatching {
                objectMapper.readTree(rawBody)
                    ?.path(fieldName)
                    ?.takeIf { !it.isMissingNode && !it.isNull }
            }.getOrNull()
        }

        private fun JsonNode.toErrorCode(): Any? =
            when {
                isIntegralNumber -> if (canConvertToInt()) intValue() else longValue()
                isNumber -> numberValue()
                isTextual -> asText().trim().takeIf(String::isNotBlank)
                else -> toString().takeIf(String::isNotBlank)
            }
    }
}
