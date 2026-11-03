package uz.davrbank.compliance.integration.davrhub

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import uz.davrbank.compliance.config.ApplicationProperties
import java.time.Duration
import java.util.concurrent.TimeoutException

@Service
class DavrHubTokenService(
    @Qualifier(DavrHubConstants.WEB_CLIENT_BEAN)
    private val webClient: WebClient,
    private val properties: ApplicationProperties,
    private val tokenStore: DavrHubTokenStore,
) {

    fun getAuthorizationHeader(): String = getAccessToken().authorizationHeaderValue()

    fun getAccessToken(): DavrHubAccessToken =
        findCachedToken() ?: loadFreshAccessToken()

    fun evictAccessToken() {
        tokenStore.evictToken()
        logger.info("DavrHub access token evicted")
    }

    @Synchronized
    private fun loadFreshAccessToken(): DavrHubAccessToken =
        findCachedToken() ?: requestToken().toAccessToken().also(::saveToken)

    private fun requestToken(): DavrHubTokenResponse {
        val davrHub = properties.davrHub
        validateTokenConfiguration(davrHub)

        val bodyBuilder = MultipartBodyBuilder().apply {
            part("grant_type", davrHub.grantType)
            part("username", davrHub.clientUsername)
            part("password", davrHub.clientPassword)
        }

        logger.info("DavrHub token request started uri='{}'", DavrHubConstants.TOKEN_URI)

        return try {
            webClient.post()
                .uri(DavrHubConstants.TOKEN_URI)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .headers { headers ->
                    headers.setBasicAuth(davrHub.basicAuthUsername, davrHub.basicAuthPassword)
                }
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(DavrHubTokenResponse::class.java)
                .timeout(davrHub.requestTimeout)
                .onErrorMap(TimeoutException::class.java) { exception ->
                    DavrHubCommunicationException("DavrHub token request timed out", cause = exception)
                }
                .blockOptional()
                .orElseThrow {
                    DavrHubCommunicationException("DavrHub token response was empty")
                }
        } catch (exception: WebClientResponseException) {
            logger.error(
                "DavrHub token endpoint error status='{}', rawBody='{}'",
                exception.statusCode,
                exception.responseBodyAsString,
                exception,
            )
            throw DavrHubCommunicationException(
                message = "DavrHub token endpoint responded with status ${exception.statusCode}",
                downstreamStatusCode = exception.statusCode,
                responseBody = exception.responseBodyAsString,
                cause = exception,
            )
        } catch (exception: WebClientRequestException) {
            logger.error(
                "DavrHub token request failed message='{}'",
                exception.message,
                exception,
            )
            throw DavrHubCommunicationException(
                message = "DavrHub token request failed: ${exception.message}",
                cause = exception,
            )
        }
    }

    private fun findCachedToken(): DavrHubAccessToken? = tokenStore.getToken()

    private fun saveToken(accessToken: DavrHubAccessToken) {
        val ttl = Duration.ofSeconds(resolveTtl(accessToken.expiresInSeconds))
        tokenStore.storeToken(accessToken, ttl)
        logger.info("DavrHub access token stored ttlSeconds={}", ttl.seconds)
    }

    private fun resolveTtl(expiresInSeconds: Long): Long {
        val tokenDuration = Duration.ofSeconds(expiresInSeconds.coerceAtLeast(1))
        val adjusted = tokenDuration.minus(properties.davrHub.tokenExpirySkew)
        val ttl = if (adjusted.isNegative || adjusted.isZero) tokenDuration else adjusted

        return ttl.seconds.coerceAtLeast(1)
    }

    private fun validateTokenConfiguration(davrHub: ApplicationProperties.DavrHubProperties) {
        val missing = buildList {
            if (davrHub.clientUsername.isBlank()) add("application.davr-hub.client-username")
            if (davrHub.clientPassword.isBlank()) add("application.davr-hub.client-password")
            if (davrHub.basicAuthUsername.isBlank()) add("application.davr-hub.basic-auth-username")
            if (davrHub.basicAuthPassword.isBlank()) add("application.davr-hub.basic-auth-password")
            if (davrHub.grantType.isBlank()) add("application.davr-hub.grant-type")
        }

        if (missing.isNotEmpty()) {
            throw DavrHubCommunicationException(
                "DavrHub token configuration is incomplete: ${missing.joinToString(", ")}"
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DavrHubTokenService::class.java)
    }
}