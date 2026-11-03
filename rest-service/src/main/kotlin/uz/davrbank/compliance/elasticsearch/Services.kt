package uz.davrbank.compliance.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.SearchRequest
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uz.davrbank.compliance.dropEsChars
import kotlin.math.log


interface ESService {
    fun save(document: NewComplianceData)
    fun deleteAllData()
    fun delete(ids: List<String>)
}


interface ESCheckService {
    fun findIndividuals(firstname: String, lastname: String, pageable: Pageable): Page<ESHits>
    fun findOrganizations(organizationName: String,  pageable: Pageable): Page<ESHits>

    //
    fun findIndividualsInternal(firstname: String, lastname: String): ESHits
    fun findOrganizationsInternal(organizationName: String): ESHits

    // global search uchun

    fun search(firstname: String, lastname: String, pageable: Pageable, target: SearchTarget): Page<ESHits>
}

@Service
class ESCheckServiceImpl(
    private val complianceSearchQueryBuilder: ComplianceSearchQueryBuilder,
    private val elasticsearchClient: ElasticsearchClient
) : ESCheckService {
    override fun findIndividuals(
        firstname: String,
        lastname: String,
        pageable: Pageable
    ): Page<ESHits> {
        return complianceSearchQueryBuilder.searchIndividuals(firstname, lastname, pageable)
    }

    override fun findOrganizations(
        organizationName: String,
        pageable: Pageable
    ): Page<ESHits> {
        return complianceSearchQueryBuilder.searchOrganizations(organizationName, pageable)
    }

    override fun findIndividualsInternal(
        firstname: String,
        lastname: String
    ): ESHits {
        return complianceSearchQueryBuilder.searchIndividualsInternal(firstname, lastname)
    }

    override fun findOrganizationsInternal(organizationName: String): ESHits {
        return complianceSearchQueryBuilder.searchOrganizationsInternal(organizationName)
    }

    override fun search(
        firstname: String, lastname: String,
        pageable: Pageable,
        target: SearchTarget
    ): Page<ESHits> {
        return complianceSearchQueryBuilder.search(firstname, lastname, target, pageable)
    }

}

@Service
class ESServiceImpl(
    private val repository: ComplianceDataRepository,
    private val elasticsearchClient: ElasticsearchClient
) : ESService {
    private val logger = LoggerFactory.getLogger(ESService::class.java)
    override fun deleteAllData() {
        logger.info("Deleting all data")

        val response = elasticsearchClient.indices().delete{
            it.index("compliance")
                .ignoreUnavailable(true)
        }
        if (response.acknowledged()){
            logger.info("Deleted successfully")
        }
        repository.deleteAll()
        logger.info("finished")
    }

    override fun delete(ids: List<String>) {
        repository.deleteAllById(ids)
    }

    override fun save(document: NewComplianceData) {
        val firstnameCopy = document.firstname.dropEsChars()
        val lastnameCopy = document.lastname.dropEsChars()
        val fullNameCopy = document.fullName.dropEsChars()

        val esDoc = NewComplianceData(
            firstnameCopy,
            lastnameCopy,
            fullNameCopy,
            document.extLookUpId,
            document.type,
            document.complianceType,
            id = document.id,
            document.dataType
        )
        repository.save(esDoc)
    }

}
