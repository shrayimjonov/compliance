package uz.davrbank.compliance.service.impl

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.IndexRequest
import mu.KotlinLogging
import org.springframework.stereotype.Service
import uz.davrbank.compliance.config.ApplicationProperties
import uz.davrbank.compliance.domain.Compliance
import uz.davrbank.compliance.exception.ServiceUnavailableException
import uz.davrbank.compliance.service.ComplianceSearchIndexService
import uz.davrbank.compliance.util.TextNormalizer

@Service
class ComplianceSearchIndexServiceImpl(
    private val elasticsearchClient: ElasticsearchClient,
    private val properties: ApplicationProperties,
    private val textNormalizer: TextNormalizer,
) : ComplianceSearchIndexService {

    override fun save(compliance: Compliance) {
        val document = compliance.toSearchDocument()
        val request = IndexRequest.of<ComplianceSearchDocument> { builder ->
            builder
                .index(indexName())
                .id(document.id)
                .document(document)
        }

        try {
            elasticsearchClient.index(request)
        } catch (ex: Exception) {
            logger.error(ex) { "Failed to index local compliance id='${compliance.id}'" }
            throw ServiceUnavailableException(
                message = "Elasticsearch index update is currently unavailable.",
                code = ELASTICSEARCH_UNAVAILABLE,
                cause = ex,
            )
        }
    }

    override fun delete(complianceId: String) {
        val index = indexName()

        try {
            val exists = elasticsearchClient.exists { request ->
                request.index(index).id(complianceId)
            }.value()

            if (exists) {
                elasticsearchClient.delete { request ->
                    request.index(index).id(complianceId)
                }
            }
        } catch (ex: Exception) {
            logger.error(ex) { "Failed to delete local compliance index document id='$complianceId'" }
            throw ServiceUnavailableException(
                message = "Elasticsearch index update is currently unavailable.",
                code = ELASTICSEARCH_UNAVAILABLE,
                cause = ex,
            )
        }
    }

    private fun Compliance.toSearchDocument() = ComplianceSearchDocument(
        id = id,
        name = name.toIndexValue(),
        firstname = firstName.toIndexValue(),
        lastname = lastName.toIndexValue(),
        fullName = fullName.toIndexValue(),
        extLookUpId = lookupId ?: id,
        type = type.name,
        complianceType = complianceType.name,
        dataType = dataType.name,
    )

    private fun String?.toIndexValue(): String = this
        ?.let(textNormalizer::compact)
        .orEmpty()

    private fun indexName(): String {
        val index = properties.elastic.index.trim()
        if (index.isBlank()) {
            throw ServiceUnavailableException(
                message = "Elasticsearch index is not configured.",
                code = ELASTICSEARCH_UNAVAILABLE,
            )
        }

        return index
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val ELASTICSEARCH_UNAVAILABLE = "ELASTICSEARCH_UNAVAILABLE"
    }

    private data class ComplianceSearchDocument(
        val id: String,
        val name: String,
        val firstname: String,
        val lastname: String,
        val fullName: String,
        val extLookUpId: String,
        val type: String,
        val complianceType: String,
        val dataType: String,
        val birth: String? = null,
        val region: String = "",
    )
}
