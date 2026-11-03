package uz.davrbank.compliance.config

import io.minio.MinioClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioConfig {

    @Bean
    @ConditionalOnProperty(prefix = "application.min-io", name = ["url"])
    fun minioClient(properties: ApplicationProperties): MinioClient =
        MinioClient.builder()
            .endpoint(properties.minIo.url)
            .credentials(properties.minIo.accessKey, properties.minIo.secretKey)
            .build()
}
