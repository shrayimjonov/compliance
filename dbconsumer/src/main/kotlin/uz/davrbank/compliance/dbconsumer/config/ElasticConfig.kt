package uz.davrbank.compliance.dbconsumer.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration

@EnableAutoConfiguration(exclude = [ElasticsearchRestClientAutoConfiguration::class])
@Configuration
class ElasticConfig(
    @Value("\${elastic.host}")
    val elasticHost: String,
    @Value("\${elastic.username}")
    val username: String,

    @Value("\${elastic.ssl-fingerprint}")
    val sslFingerPrint: String,
    @Value("\${elastic.password}")
    val password: String
) : ElasticsearchConfiguration() {

    override fun clientConfiguration(): ClientConfiguration {
        return ClientConfiguration.builder()
            .connectedTo(elasticHost)
            .usingSsl(sslFingerPrint)
            .withBasicAuth(username, password)
            .build()
    }
}

// TODO Only test
//@Configuration
//class ElasticConfig(
//    @Value("\${elastic.host}")
//    val elasticHost: String,
//    @Value("\${elastic.username}")
//    val username: String,
//    @Value("\${elastic.password}")
//    val password: String
//) : ElasticsearchConfiguration() {
//
//    override fun clientConfiguration(): ClientConfiguration {
//
//        return ClientConfiguration.builder()
//            .connectedTo(elasticHost)
//            .withBasicAuth(username, password)
//            .build()
//    }
//}