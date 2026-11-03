package uz.davrbank.compliance.dbconsumer.service.impl

import co.elastic.clients.elasticsearch.ElasticsearchClient
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import uz.davrbank.compliance.dbconsumer.domain.elasticsearch.NewComplianceData
import uz.davrbank.compliance.dbconsumer.domain.postgresql.Compliance
import uz.davrbank.compliance.dbconsumer.repository.elasticsearch.ComplianceDataRepository
import uz.davrbank.compliance.dbconsumer.repository.postgresql.ComplianceRepository
import uz.davrbank.compliance.dbconsumer.util.dropEsChars

@Service
class ReindexService(
    private val complianceRepository: ComplianceRepository,
    private val esRepository: ComplianceDataRepository,
    private val elasticsearchClient: ElasticsearchClient
) {
    private val logger = KotlinLogging.logger {}
    private val BATCH_SIZE = 1000

    @PostConstruct
    fun runOnStartup() {
        logger.info { "Starting ReindexService" }
        Thread {
//            Thread.sleep(5000)
            try {
                reindex()
            } catch (e: Exception) {
                logger.error(e) { "Reindex failed" }
            }
        }.also { it.isDaemon = true }.start()
    }

    fun reindex() {
        logger.info { "=== Reindex Starting ===" }
        val startTime = System.currentTimeMillis()

        try {
            setRefreshInterval("-1")
        } catch (e: Exception) {
            logger.warn { "Could not set refresh interval: ${e.message}" }
        }

        try {
            var page = 0
            var totalInserted = 0
            var totalUpdated = 0
            var totalSkipped = 0

            do {
                val idPage = complianceRepository.findAllIds(PageRequest.of(page, BATCH_SIZE))
                if (idPage.isEmpty) break

                val ids = idPage.content

                val existingEsDocs = esRepository.findAllById(ids).associateBy { it.id }

                val newIds = ids.filter { it !in existingEsDocs }
                val updateIds = ids.filter { id ->
                    val esDoc = existingEsDocs[id]
                    esDoc != null && esDoc.type == "Individual" && esDoc.birth.isNullOrBlank()
                }

                if (newIds.isNotEmpty()) {
                    val batch = complianceRepository.findAllByIdInWithIdentifications(newIds)
                    val esDocs = batch.map { it.toEsDocument() }
                    esRepository.saveAll(esDocs)
                    totalInserted += esDocs.size
                }

                if (updateIds.isNotEmpty()) {
                    val batch = complianceRepository.findAllByIdInWithIdentifications(updateIds)
                    val esDocs = batch.map { it.toEsDocument() }
                    esRepository.saveAll(esDocs)
                    totalUpdated += esDocs.size
                }

                totalSkipped += ids.size - newIds.size - updateIds.size

                logger.info {
                    "Progress: inserted=$totalInserted, updated=$totalUpdated, skipped=$totalSkipped"
                }
                page++

            } while (idPage.hasNext())

            val elapsed = (System.currentTimeMillis() - startTime) / 1000
            logger.info {
                "=== Reindex tugadi: inserted=$totalInserted, updated=$totalUpdated, skipped=$totalSkipped, ${elapsed}s ==="
            }

        } finally {
            try {
                setRefreshInterval("1s")
            } catch (e: Exception) {
                logger.warn { "Could not restore refresh interval: ${e.message}" }
            }
        }
    }

    private fun setRefreshInterval(interval: String) {
        elasticsearchClient.indices().putSettings {
            it.index("compliance_data_v4")
                .settings { s ->
                    s.refreshInterval { r -> r.time(interval) }
                }
        }
    }

    private fun Compliance.toEsDocument(): NewComplianceData {
        val firstnameCopy = (firstName ?: "").dropEsChars()
        val lastnameCopy = lastName.dropEsChars()
        val fullNameCopy = fullName

        val identNum = identifications
            .firstOrNull()
            ?.identificationNumber ?: ""

        val region = countryAssociations
            .firstOrNull()
            ?.administrativeUnitName ?: ""

        val birth = entityDOBs.firstOrNull()
            ?.birthDate ?: ""

        return NewComplianceData(
            firstname = firstnameCopy,
            lastname = lastnameCopy,
            fullName = fullNameCopy,
            extLookUpId = lookupId ?: "",
            type = type.name,
            complianceType = complianceType,
            id = id,
            dataType = dataType,
            birth = birth as String?,
            region = region,
            entityAdverseMedia = false,
            name = name,
            identificationNumber = identNum
        )
    }
}