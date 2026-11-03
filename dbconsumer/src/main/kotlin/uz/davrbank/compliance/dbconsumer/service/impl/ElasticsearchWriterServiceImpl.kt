package uz.davrbank.compliance.dbconsumer.service.impl

import co.elastic.clients.elasticsearch.ElasticsearchClient
import mu.KotlinLogging
import org.springframework.stereotype.Service
import uz.davrbank.compliance.dbconsumer.domain.elasticsearch.NewComplianceData
import uz.davrbank.compliance.dbconsumer.repository.elasticsearch.ComplianceDataRepository
import uz.davrbank.compliance.dbconsumer.service.ElasticsearchWriterService
import uz.davrbank.compliance.dbconsumer.util.dropEsChars

@Service
class ElasticsearchWriterServiceImpl(
    private val repository: ComplianceDataRepository,
    private val elasticsearchClient: ElasticsearchClient
) : ElasticsearchWriterService {

    private val logger = KotlinLogging.logger {}

    override fun save(document: NewComplianceData) {
        val firstnameCopy = document.firstname.dropEsChars()
        val lastnameCopy  = document.lastname.dropEsChars()
        val fullNameCopy  = document.fullName.dropEsChars()

        val esDoc = NewComplianceData(
            firstname = firstnameCopy,
            lastname = lastnameCopy,
            fullName = fullNameCopy,
            extLookUpId = document.extLookUpId,
            type = document.type,
            complianceType = document.complianceType,
            id = document.id,
            dataType = document.dataType,
            birth = document.birth,
            region = document.region,
            entityAdverseMedia = document.entityAdverseMedia,
            name = document.name,
            identificationNumber = document.identificationNumber
        )
        repository.save(esDoc)
        logger.debug { "Saved to Elasticsearch: id=${document.id}" }
    }

    override fun delete(ids: List<String>) {
        repository.deleteAllById(ids)
        logger.debug { "Deleted ${ids.size} documents from Elasticsearch" }
    }

    override fun deleteAllData() {
        logger.info("Deleting all Elasticsearch data...")
        elasticsearchClient.indices().delete {
            it.index("compliance").ignoreUnavailable(true)
        }
        repository.deleteAll()
        logger.info("All Elasticsearch data deleted")
    }
}