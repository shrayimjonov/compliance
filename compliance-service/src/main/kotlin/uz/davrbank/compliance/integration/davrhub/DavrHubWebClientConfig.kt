package uz.davrbank.compliance.integration.davrhub

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import uz.davrbank.compliance.config.ApplicationProperties
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class DavrHubWebClientConfig(
    private val properties: ApplicationProperties,
    private val objectMapper: ObjectMapper,
) {

    @Bean(DavrHubConstants.WEB_CLIENT_BEAN)
    fun davrHubWebClient(
        @Lazy tokenService: DavrHubTokenService,
    ): WebClient {
        val davrHub = properties.davrHub
        validateBaseUrl(davrHub.baseUrl)
        validateDuration(davrHub.connectTimeout, "application.davr-hub.connect-timeout")
        validateDuration(davrHub.readTimeout, "application.davr-hub.read-timeout")
        validateDuration(davrHub.writeTimeout, "application.davr-hub.write-timeout")

        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, davrHub.connectTimeout.toMillis().toConnectTimeoutMillis())
            .responseTimeout(davrHub.readTimeout)
            .doOnConnected { connection ->
                connection
                    .addHandlerLast(ReadTimeoutHandler(davrHub.readTimeout.toMillis(), TimeUnit.MILLISECONDS))
                    .addHandlerLast(WriteTimeoutHandler(davrHub.writeTimeout.toMillis(), TimeUnit.MILLISECONDS))
            }

        return WebClient.builder()
            .baseUrl(davrHub.baseUrl.trim())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(DavrHubConstants.SERVICE_NAME_HEADER, davrHub.serviceName)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .filter(bearerTokenFilter(tokenService))
            .filter(outboundLoggingFilter())
            .build()
    }

    private fun bearerTokenFilter(tokenService: DavrHubTokenService): ExchangeFilterFunction =
        ExchangeFilterFunction.ofRequestProcessor { request ->
            if (request.url().path == DavrHubConstants.TOKEN_URI) {
                return@ofRequestProcessor Mono.just(request)
            }

            val authorizedRequest = ClientRequest.from(request)
                .headers { headers ->
                    headers.set(HttpHeaders.AUTHORIZATION, tokenService.getAuthorizationHeader())
                }
                .build()
            Mono.just(authorizedRequest)
        }

    private fun outboundLoggingFilter(): ExchangeFilterFunction =
        ExchangeFilterFunction.ofRequestProcessor { request ->
            logger.info(
                "DavrHub outbound request metadata={}",
                toJsonLog(
                    mapOf(
                        "method" to request.method().name(),
                        "url" to request.url().toString(),
                    )
                )
            )
            Mono.just(request)
        }.andThen(
            ExchangeFilterFunction.ofResponseProcessor { response ->
                logger.info(
                    "DavrHub outbound response metadata={}",
                    toJsonLog(
                        mapOf(
                            "status" to response.statusCode().value(),
                        )
                    )
                )
                Mono.just(response)
            }
        )

    private fun validateBaseUrl(baseUrl: String) {
        require(baseUrl.isNotBlank()) {
            "Property 'application.davr-hub.base-url' must be configured."
        }
        require(!UNRESOLVED_PLACEHOLDER_PATTERN.containsMatchIn(baseUrl)) {
            "Property 'application.davr-hub.base-url' is unresolved. Check Vault or environment loading."
        }
    }

    private fun validateDuration(duration: Duration, propertyName: String) {
        require(!duration.isNegative && !duration.isZero) {
            "Property '$propertyName' must be greater than zero."
        }
    }

    private fun Long.toConnectTimeoutMillis(): Int =
        coerceIn(1, Int.MAX_VALUE.toLong()).toInt()

    private fun toJsonLog(value: Any): String =
        runCatching { objectMapper.writeValueAsString(value) }
            .getOrElse { exception ->
                logger.warn("Unable to serialize DavrHub log metadata type='{}'", value::class.java.name, exception)
                value.toString()
            }

    companion object {
        private val logger = LoggerFactory.getLogger(DavrHubWebClientConfig::class.java)
        private val UNRESOLVED_PLACEHOLDER_PATTERN = Regex("""\$\{[^}]+}""")
    }
}