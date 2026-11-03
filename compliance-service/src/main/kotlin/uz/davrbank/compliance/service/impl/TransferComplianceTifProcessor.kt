package uz.davrbank.compliance.service.impl

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import uz.davrbank.compliance.dto.request.filter.BaseFilter
import uz.davrbank.compliance.dto.request.filter.OrgSearch
import uz.davrbank.compliance.dto.request.filter.OrgType
import uz.davrbank.compliance.enums.LogSource
import uz.davrbank.compliance.enums.TransferComplianceStatus
import uz.davrbank.compliance.repository.TransferComplianceCheckRepository
import uz.davrbank.compliance.service.ApiLogEvent
import uz.davrbank.compliance.service.ApiLogService
import uz.davrbank.compliance.service.ElasticsearchSearchService
import java.util.*

@Service
class TransferComplianceTifProcessor(
    private val elasticsearchSearchService: ElasticsearchSearchService,
    private val apiLogService: ApiLogService,
    private val transferComplianceCheckRepository: TransferComplianceCheckRepository,
) {

    @Async("absCheckerExecutor")
    fun process(command: TifSearchCommand) {
        try {
            runInternal(command)
        } catch (ex: Exception) {
            logger.error(ex) {
                "Unexpected failure during TIF search for transferCheckId=${command.transferCheckId}. " +
                    "Marking as CHECK_ERROR."
            }
            updateStatusFromSent(command.transferCheckId, TransferComplianceStatus.CHECK_ERROR)
        }
    }

    private fun runInternal(command: TifSearchCommand) {
        val searchInputs = listOf(
            CLIENT_SEARCH_KEY to buildKeyword(command.clientName, command.clientTin),
            FOREIGN_PARTNER_SEARCH_KEY to buildKeyword(command.foreignPartnerName, command.foreignPartnerTin),
        ).filter { (_, keyword) -> keyword.isNotBlank() }

        if (searchInputs.isEmpty()) {
            logger.warn {
                "Skipping TIF search for transferCheckId=${command.transferCheckId}: " +
                    "no client/partner name or INN to search."
            }
            return
        }

        val startedAt = System.currentTimeMillis()

        val responseBody = linkedMapOf<String, Any>()
        val queryParams = mutableListOf<String>()
        var anyFailed = false

        searchInputs.forEach { (key, keyword) ->
            val (result, failed) = runSearch(command.transferCheckId, keyword)
            responseBody[key] = result
            queryParams += "$key=$keyword"
            anyFailed = anyFailed || failed
        }

        apiLogService.save(
            ApiLogEvent(
                requestId = command.transferCheckId.toString(),
                username = command.username,
                method = SEARCH_METHOD,
                endpoint = "$TIF_ENDPOINT/${command.checkNumber}",
                queryParams = queryParams.joinToString("&"),
                requestBody = mapOf(
                    "transferCheckId" to command.transferCheckId.toString(),
                    "externalId" to command.externalId.toString(),
                    "checkNumber" to command.checkNumber,
                    "clientName" to command.clientName.orEmpty(),
                    "clientTin" to command.clientTin.orEmpty(),
                    "foreignPartnerName" to command.foreignPartnerName.orEmpty(),
                    "foreignPartnerTin" to command.foreignPartnerTin.orEmpty(),
                ),
                responseBody = responseBody,
                source = LogSource.TIF,
                statusCode = if (anyFailed) STATUS_UNAVAILABLE else STATUS_OK,
                timeTakenMs = System.currentTimeMillis() - startedAt,
            )
        )

        if (anyFailed) {
            logger.warn {
                "TIF search failed for transferCheckId=${command.transferCheckId}. Marking as CHECK_ERROR."
            }
            updateStatusFromSent(command.transferCheckId, TransferComplianceStatus.CHECK_ERROR)
            return
        }

        updateStatusFromSent(command.transferCheckId, TransferComplianceStatus.CHECKED)
        logger.info {
            "Stored TIF search result for transferCheckId=${command.transferCheckId}, checkNumber=${command.checkNumber}"
        }
    }

    private fun buildKeyword(name: String?, inn: String?): String =
        listOf(name, inn)
            .mapNotNull { it?.trim()?.takeIf(String::isNotBlank) }
            .joinToString(" ")

    private fun updateStatusFromSent(transferCheckId: UUID, newStatus: TransferComplianceStatus) {
        runCatching {
            val updated = transferComplianceCheckRepository.updateStatusIfCurrent(
                transferCheckId,
                TransferComplianceStatus.SENT,
                newStatus,
            )
            if (updated == 0) {
                logger.info {
                    "Status of transferCheckId=$transferCheckId left unchanged (no longer SENT); wanted $newStatus."
                }
            }
        }.onFailure { ex ->
            logger.error(ex) { "Failed to update status of transferCheckId=$transferCheckId to $newStatus" }
        }
    }

    private fun runSearch(transferCheckId: UUID, keyword: String): Pair<Any, Boolean> = runCatching {
        val filter = BaseFilter(
            requestId = transferCheckId.toString(),
            keyword = keyword,
            params = OrgSearch(orgType = OrgType.J),
            pagination = BaseFilter.Pagination(page = 0, size = TIF_MAX_HITS),
        )
        elasticsearchSearchService.search(filter) as Any to false
    }.getOrElse { ex ->
        logger.error(ex) { "TIF Elasticsearch search failed for transferCheckId=$transferCheckId, keyword='$keyword'" }
        mapOf(
            "error" to (ex.message ?: ex.javaClass.simpleName),
            "exception" to ex.javaClass.simpleName,
        ) as Any to true
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val TIF_MAX_HITS = 100
        private const val STATUS_OK = 200
        private const val STATUS_UNAVAILABLE = 503
        private const val SEARCH_METHOD = "SEARCH"
        private const val TIF_ENDPOINT = "/transfer-compliance-check/tif"
        private const val CLIENT_SEARCH_KEY = "clientSearch"
        private const val FOREIGN_PARTNER_SEARCH_KEY = "foreignPartnerSearch"
    }
}

data class TifSearchCommand(
    val transferCheckId: UUID,
    val externalId: UUID,
    val checkNumber: String,
    val clientName: String?,
    val clientTin: String?,
    val foreignPartnerName: String?,
    val foreignPartnerTin: String?,
    val username: String?,
)
