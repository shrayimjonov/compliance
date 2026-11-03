package uz.davrbank.compliance.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "application")
data class ApplicationProperties(
    val elastic: ElasticProperties = ElasticProperties(),
    val davrHub: DavrHubProperties = DavrHubProperties(),
    val minIo: MinIoProperties = MinIoProperties(),
) {
    data class MinIoProperties(
        val url: String = "",
        val bucket: String = "",
        val accessKey: String = "",
        val secretKey: String = "",
    )

    data class ElasticProperties(
        val host: String = "",
        val index: String = "",
        val username: String = "",
        val password: String = "",
        val sslFingerprint: String = "",
        val maxPageSize: Int = 1000,
        val searchTimeout: String = "5s",
        val requestCache: Boolean = true,
        val trackTotalHitsUpTo: Int = 10_000,
        val maxWildcardTokens: Int = 2,
        val entityGuidField: String = "consolidatedSanctionEntityGuId",
    )

    data class DavrHubProperties(
        val baseUrl: String = "",
        val clientUsername: String = "",
        val clientPassword: String = "",
        val basicAuthUsername: String = "",
        val basicAuthPassword: String = "",
        val connectTimeout: Duration = Duration.ofSeconds(10),
        val readTimeout: Duration = Duration.ofSeconds(10),
        val writeTimeout: Duration = Duration.ofSeconds(5),
        val requestTimeout: Duration = Duration.ofSeconds(15),
        val grantType: String = "password",
        val tokenExpirySkew: Duration = Duration.ofSeconds(300),
        val logBodyLimit: Int = 20_000,
        val serviceName: String = "compliance-service",
    )
}