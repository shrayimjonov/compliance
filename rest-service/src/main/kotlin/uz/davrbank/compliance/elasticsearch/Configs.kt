package uz.davrbank.compliance.elasticsearch

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration


@Configuration
class ElasticConfigs(
    @Value("\${elastic.host}") // localhost:9200
    val elasticHost: String,
    @Value("\${elastic.ssl-fingerprint}")
    val sslFingerPrint: String,
    @Value("\${elastic.username}")
    val username: String,
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

//    @Bean
//    fun elasticsearchTemplate(client: ElasticsearchClient): ElasticsearchTemplate {
//        val converter = MappingElasticsearchConverter(SimpleElasticsearchMappingContext())
//        val mapper = ScoreResultsMapper(converter.mappingContext)
//        val elasticSearchTemplate = ElasticsearchTemplate(client, converter)
//    }
}
