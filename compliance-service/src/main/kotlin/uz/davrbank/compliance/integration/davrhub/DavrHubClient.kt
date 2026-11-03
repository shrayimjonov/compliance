package uz.davrbank.compliance.integration.davrhub

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uz.davrbank.compliance.config.ApplicationProperties
import java.util.concurrent.TimeoutException

@Component
class DavrHubClient(
    @Qualifier(DavrHubConstants.WEB_CLIENT_BEAN)
    private val webClient: WebClient,
    private val tokenService: DavrHubTokenService,
    private val properties: ApplicationProperties,
    private val objectMapper: ObjectMapper,
) {

    fun <T : Any> exchange(
        method: HttpMethod,
        uri: String,
        requestBody: Any?,
        responseType: Class<T>,
    ): T? {
        val response = readResponse(
            rawResponse = execute(method, uri, requestBody).block(),
            responseType = responseType,
        )
        logMappedResponse(method, uri, response)

        return response
    }

    private fun execute(
        method: HttpMethod,
        uri: String,
        requestBody: Any?,
    ): Mono<String> = execute(method, uri, requestBody, retryUnauthorized = true)

    private fun execute(
        method: HttpMethod,
        uri: String,
        requestBody: Any?,
        retryUnauthorized: Boolean,
    ): Mono<String> {
        logger.info(
            "DavrHub request method='{}', uri='{}', body={}",
            method.name(),
            uri,
            serializeForLog(requestBody),
        )

        return buildRequest(method, uri, requestBody)
            .retrieve()
            .bodyToMono(String::class.java)
            .defaultIfEmpty("")
            .doOnNext { rawResponse ->
                logger.info(
                    "DavrHub raw response method='{}', uri='{}', body={}",
                    method.name(),
                    uri,
                    rawResponse.limitForLog(),
                )
            }
            .timeout(properties.davrHub.requestTimeout)
            .onErrorResume(WebClientResponseException.Unauthorized::class.java) { exception ->
                logDavrHubErrorResponse(method, uri, exception)
                if (!retryUnauthorized) {
                    Mono.error(toDavrHubCommunicationException(exception))
                } else {
                    tokenService.evictAccessToken()
                    execute(method, uri, requestBody, retryUnauthorized = false)
                }
            }
            .onErrorMap(WebClientResponseException::class.java) { exception ->
                logDavrHubErrorResponse(method, uri, exception)
                toDavrHubCommunicationException(exception)
            }
            .onErrorMap(WebClientRequestException::class.java) { exception ->
                logger.error(
                    "DavrHub request failed method='{}', uri='{}', message='{}'",
                    method.name(),
                    uri,
                    exception.message,
                    exception,
                )
                DavrHubCommunicationException(
                    message = "DavrHub request failed: ${exception.message}",
                    cause = exception,
                )
            }
            .onErrorMap(TimeoutException::class.java) { exception ->
                logger.error(
                    "DavrHub request timed out method='{}', uri='{}'",
                    method.name(),
                    uri,
                    exception,
                )
                DavrHubCommunicationException("DavrHub request timed out", cause = exception)
            }
    }

    private fun <T : Any> readResponse(rawResponse: String?, responseType: Class<T>): T? {
        if (rawResponse.isNullOrBlank()) {
            return null
        }

        return try {
            val rootNode = objectMapper.readTree(rawResponse)
            val node = rootNode.singleResponseNode()
            if (node == null || node.isNull) {
                null
            } else {
                objectMapper.treeToValue(node, responseType)
            }
        } catch (exception: Exception) {
            throw DavrHubCommunicationException("Failed to decode DavrHub response", cause = exception)
        }
    }

    private fun JsonNode.singleResponseNode(): JsonNode? =
        if (isArray) {
            if (isEmpty) null else this[0]
        } else {
            this
        }

    private fun buildRequest(
        method: HttpMethod,
        uri: String,
        requestBody: Any?,
    ): WebClient.RequestHeadersSpec<*> {
        val requestSpec = webClient.method(method).uri(uri)

        return if (requestBody == null) {
            requestSpec
        } else {
            requestSpec.bodyValue(requestBody)
        }
    }

    private fun toDavrHubCommunicationException(exception: WebClientResponseException): DavrHubCommunicationException =
        DavrHubCommunicationException(
            message = "DavrHub responded with status ${exception.statusCode}",
            downstreamStatusCode = exception.statusCode,
            responseBody = exception.responseBodyAsString,
            cause = exception,
        )

    private fun logMappedResponse(method: HttpMethod, uri: String, response: Any?) {
        logger.info(
            "DavrHub mapped response method='{}', uri='{}', body={}",
            method.name(),
            uri,
            serializeForLog(response),
        )
    }

    private fun logDavrHubErrorResponse(
        method: HttpMethod,
        uri: String,
        exception: WebClientResponseException,
    ) {
        logger.error(
            "DavrHub error response method='{}', uri='{}', status='{}', rawBody={}",
            method.name(),
            uri,
            exception.statusCode,
            exception.responseBodyAsString.limitForLog(),
            exception,
        )
    }

    private fun serializeForLog(value: Any?): String =
        when (value) {
            null -> "null"
            is String -> value.limitForLog()
            else -> try {
                objectMapper.writeValueAsString(value).limitForLog()
            } catch (exception: JsonProcessingException) {
                logger.warn(
                    "Unable to serialize DavrHub log payload type='{}'",
                    value::class.java.name,
                    exception,
                )
                value.toString().limitForLog()
            }
        }

    private fun String.limitForLog(): String {
        val limit = properties.davrHub.logBodyLimit
        if (limit <= 0) {
            return "[omitted,length=$length]"
        }

        return if (length <= limit) {
            this
        } else {
            "${take(limit)}...[truncated,length=$length]"
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DavrHubClient::class.java)
    }
}
