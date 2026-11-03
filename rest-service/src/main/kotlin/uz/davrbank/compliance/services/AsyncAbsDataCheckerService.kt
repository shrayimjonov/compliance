package uz.davrbank.compliance.services

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withPermit
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uz.davrbank.compliance.*
import uz.davrbank.compliance.backup.Compliance
import uz.davrbank.compliance.backup.ComplianceEntityPRepository
import uz.davrbank.compliance.backup.Type
import uz.davrbank.compliance.elasticsearch.ESCheckService
import uz.davrbank.compliance.elasticsearch.ESHits


@Service
class AsyncAbsDataCheckerService(
    private val repository: AbsDataCheckerRepository,
    private val esCheckService: ESCheckService,
    private val complianceRepository: ComplianceEntityPRepository,
    private val relatedDataProcessor: EntityRelatedDataProcessor,
    private val segmentProcessor: EntitySegmentProcessorService,
    private val complianceCheckDataStoreRepository: ComplianceCheckDataStoreRepository
) {
    private val logger = KotlinLogging.logger {}

    @Value("\${abs.checker.batch-size}")
    private var batchSize: Int = 50

    @Value("\${abs.checker.max-concurrent}")
    private var maxConcurrent: Int = 10


    @Scheduled(fixedDelay = 5000)
    @Async("absCheckerExecutor")
    fun scheduledCheck() {
        try {
            logger.info { "ABS DATA_CHECK STARTED" }
            val startTime = System.currentTimeMillis()
            processCreatedRecords()

            logger.info { "ABS CHECK_DATA FINISHED: ${System.currentTimeMillis() - startTime} ms" }
        } catch (e: Exception) {
            logger.error(e) { "Scheduler execution failed - will retry on next cycle" }
        }
    }

    @Scheduled(fixedDelay = 5000)
    fun saveAbsComplianceData() {

        runBlocking {
            complianceCheckDataStoreRepository.findAll().map {
                if (it.ids.isNotEmpty()) {
                    val entities = complianceRepository.findAllByIdIn(it.ids)
                    val semaphore = kotlinx.coroutines.sync.Semaphore(maxConcurrent)

                    entities.map { entity ->
                        async(Dispatchers.IO) {
                            semaphore.withPermit {
                                processEntityData(entity, it.absDataCheckerId)
                            }
                        }
                    }.awaitAll()

                }
            }
        }

        logger.info{"ABS_SAVED COMPLETE"}

    }


    private fun processCreatedRecords() {
        var processedCount = 0
        var hasMore = true
        while (hasMore) {
            val records = repository.findAllByStatus(AbsHandlerStatus.CREATED)
                .take(batchSize)

            if (records.isEmpty()) {
                hasMore = false
                continue
            }


            runBlocking {
                val results = records.map { record ->
                    async(Dispatchers.IO) {
                        processRecord(record)
                    }
                }.awaitAll()


                repository.saveAll(results)
            }

            processedCount += records.size
            logger.debug { "Processed batch of ${records.size} records (total: $processedCount)" }


            if (records.size < batchSize) {
                hasMore = false
            }
        }

        logger.info { "Processed $processedCount ABS records in total" }
    }


    private suspend fun processRecord(absDataChecker: AbsDataChecker): AbsDataChecker {
        return try {
            checkData(absDataChecker)
        } catch (e: Exception) {
            logger.error(e) { "Failed to process ABS record ${absDataChecker.id}" }

            absDataChecker.apply {
                status = AbsHandlerStatus.FAILED
                comment = "Processing failed: ${e.message}"
            }
        }
    }


    private suspend fun checkData(absDataChecker: AbsDataChecker): AbsDataChecker = coroutineScope {
        val request = absDataChecker.request
        val esHits = fetchEsHitsByRequestType(request)


        absDataChecker.apply {
            response = esHits
            status = AbsHandlerStatus.FINISHED
        }
    }


    private fun processEntityData(entity: Compliance, absDataCheckerId: String) {
        val absDataCheckerItem = AbsDataCheckerItem(absDataCheckerId = absDataCheckerId)


        segmentProcessor.processSegments(entity, absDataCheckerItem)
        relatedDataProcessor.populateRelatedData(entity, absDataCheckerItem)
    }

    private fun fetchEsHitsByRequestType(request: AbsDataCheckerRequest): ESHits {
        return when (request.type) {
            Type.Organization -> esCheckService.findOrganizationsInternal(request.orgName!!.dropEsChars())
            Type.Individual -> esCheckService.findIndividualsInternal(
                request.firstname!!.dropEsChars(),
                request.lastname!!.dropEsChars()
            )

            Type.Vessel, Type.Country, Type.Aircraft, Type.Unknown ->
                throw UnsupportedOperationException("Type ${request.type} not yet supported")
        }
    }

}


