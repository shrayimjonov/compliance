package uz.davrbank.compliance.config

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.TransportUtils
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI

@Configuration
@EnableConfigurationProperties(ApplicationProperties::class)
class ElasticsearchConfig(
    private val properties: ApplicationProperties,
) {

    @Bean(destroyMethod = "close")
    fun restClient(): RestClient {
        val elastic = properties.elastic

        require(elastic.host.isNotBlank()) {
            "Property 'application.elastic.host' must be configured."
        }
        require(!UNRESOLVED_PLACEHOLDER_PATTERN.containsMatchIn(elastic.host)) {
            "Property 'application.elastic.host' is unresolved. Check Vault or environment loading."
        }

        val endpoint = endpointUri(elastic)
        val clientBuilder = RestClient.builder(
            HttpHost(endpoint.host, endpoint.port, endpoint.scheme)
        )

        clientBuilder.setRequestConfigCallback { requestConfigBuilder ->
            requestConfigBuilder
                .setConnectTimeout(CONNECT_TIMEOUT_MS)
                .setSocketTimeout(SOCKET_TIMEOUT_MS)
        }

        if (elastic.sslFingerprint.isNotBlank() || (elastic.username.isNotBlank() && elastic.password.isNotBlank())) {
            clientBuilder.setHttpClientConfigCallback { httpClientBuilder ->
                if (elastic.sslFingerprint.isNotBlank()) {
                    httpClientBuilder.setSSLContext(
                        TransportUtils.sslContextFromCaFingerprint(elastic.sslFingerprint)
                    )
                }

                if (elastic.username.isNotBlank() && elastic.password.isNotBlank()) {
                    val credentialsProvider = BasicCredentialsProvider().apply {
                        setCredentials(
                            AuthScope.ANY,
                            UsernamePasswordCredentials(elastic.username, elastic.password)
                        )
                    }
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                }

                httpClientBuilder
            }
        }

        return clientBuilder.build()
    }

    @Bean(destroyMethod = "close")
    fun elasticsearchTransport(restClient: RestClient): ElasticsearchTransport =
        RestClientTransport(restClient, JacksonJsonpMapper())

    @Bean
    fun elasticsearchClient(transport: ElasticsearchTransport): ElasticsearchClient =
        ElasticsearchClient(transport)

    private fun endpointUri(elastic: ApplicationProperties.ElasticProperties): URI {
        val host = elastic.host.trim()
        val withScheme = if (host.contains("://")) {
            host
        } else if (elastic.sslFingerprint.isNotBlank()) {
            "https://$host"
        } else {
            "http://$host"
        }

        val uri = URI(withScheme)
        val port = when {
            uri.port > 0 -> uri.port
            uri.scheme.equals("https", ignoreCase = true) -> 443
            else -> 80
        }

        return URI(uri.scheme, uri.userInfo, uri.host, port, uri.path, uri.query, uri.fragment)
    }

    companion object {
        private val UNRESOLVED_PLACEHOLDER_PATTERN = Regex("""\$\{[^}]+}""")

        private const val SOCKET_TIMEOUT_MS = 120_000

        private const val CONNECT_TIMEOUT_MS = 10_000
    }
}
