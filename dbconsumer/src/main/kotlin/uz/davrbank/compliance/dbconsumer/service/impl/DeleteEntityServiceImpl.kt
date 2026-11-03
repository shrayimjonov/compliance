package uz.davrbank.compliance.dbconsumer.service.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uz.davrbank.compliance.dbconsumer.domain.mongodb.EntityDeleteDataStore
import uz.davrbank.compliance.dbconsumer.dto.kafka.DeleteEventKafka
import uz.davrbank.compliance.dbconsumer.repository.mongodb.EntityDeleteDataStoreRepository
import uz.davrbank.compliance.dbconsumer.service.DeleteEntityService
import uz.davrbank.compliance.dbconsumer.service.PostgresWriterService


@Service
class DeleteEntityServiceImpl(
    private val entityDeleteDataStoreRepository: EntityDeleteDataStoreRepository,
    private val postgresWriterService: PostgresWriterService
) : DeleteEntityService {

    private val logger = KotlinLogging.logger {}

    override fun saveDeleteEvent(message: DeleteEventKafka) {
        entityDeleteDataStoreRepository.save(
            EntityDeleteDataStore(
                complianceId = message.EntityGUID,
                deletedDate  = message.DateDeleted
            )
        )
        logger.debug { "Delete event saved for entityGuid=${message.EntityGUID}" }
    }

    @Scheduled(fixedDelay = 30_000)
    override fun processDeleteJob() = runBlocking {
        logger.info("STARTING DELETE ENTITY JOB...")

        val itemsToDelete = entityDeleteDataStoreRepository
            .findAllByJobFinishedIsFalseOrderByCreatedDate()
            .take(10)

        if (itemsToDelete.isEmpty()) {
            logger.info("No entities to delete. Job finished.")
            return@runBlocking
        }

        logger.info("Found ${itemsToDelete.size} entities to delete. Processing in parallel...")

        val semaphore = Semaphore(5)

        coroutineScope {
            itemsToDelete.forEach { item ->
                if (item.tryCount > 3) return@forEach
                launch(Dispatchers.IO) {
                    semaphore.withPermit {
                        try {
                            postgresWriterService.deleteEntity(item.complianceId)
                            item.jobFinished = true
                            entityDeleteDataStoreRepository.save(item)
                            logger.debug { "Successfully deleted entity id=${item.id}" }
                        } catch (ex: Exception) {
                            logger.error("Failed to delete entity id=${item.id}: ${ex.message}", ex)
                            item.tryCount += 1
                            item.errorMessage = ex.message?.take(255)
                            entityDeleteDataStoreRepository.save(item)
                        }
                    }
                }
            }
        }

        logger.info("FINISHED DELETE ENTITY JOB for ${itemsToDelete.size} items.")
    }
}