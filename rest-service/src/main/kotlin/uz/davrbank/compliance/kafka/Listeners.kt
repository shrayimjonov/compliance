package uz.davrbank.compliance.kafka

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import uz.davrbank.compliance.*
import uz.davrbank.compliance.backup.*
import uz.davrbank.compliance.elasticsearch.ESService



@Component
class KafkaListeners(
    private val esService: ESService,
    private val dataService: KafkaDataService,
    private val entityDataExtractor: EntityDataExtractor,
    private val pgBackupService: PGBackupService,
) {
    @KafkaListener(
        id = "compliance-entity-consumer",
        topics = [ENTITY],
        concurrency = "10"
    )

    fun entityListener(message: ComplianceEntityDto) {
        try {
            processEntity(message, ComplianceType.ENTITY)
        } catch (e: Exception) {
            logger.error(e) { "Failed to process ENTITY message: ${message.entityGuid}" }

            throw e
        }
    }


    @KafkaListener(
        id = "compliance-relationship-consumer",
        topics = [RELATIONSHIP],
        concurrency = "5"
    )

    fun relationshipListener(message: ComplianceEntityDto) {
        try {
            processEntity(message, ComplianceType.RELATIONSHIP)
        } catch (e: Exception) {
            logger.error(e) { "Failed to process RELATIONSHIP message: ${message.entityGuid}" }
            throw e
        }
    }

    @KafkaListener(
        id = "compliance-delete-consumer",
        topics = [ENTITY_DELETE]
    )
    fun entityDeleteListener(message: DeleteEventKafka) {
        try {
            dataService.saveEntityDeleteData(message)
        } catch (e: Exception) {
            logger.error(e) { "Failed to process DELETE message: $message" }
            throw e
        }
    }


    private fun processEntity(dto: ComplianceEntityDto, complianceType: ComplianceType) {
        val startTime = System.currentTimeMillis()


        val (complianceEntityDoc, complianceEntity) = entityDataExtractor.toComplianceModels(dto, complianceType)


        val savedEntity = pgBackupService.saveEntity(complianceEntity)
        esService.save(complianceEntityDoc)
        val additionalData = entityDataExtractor.extractAdditionalData(dto, savedEntity, complianceType)
        pgBackupService.saveAdditionalData(additionalData)

        logger.debug { "Processed ${complianceType.name} ${dto.entityGuid} in ${System.currentTimeMillis() - startTime}ms" }
    }

}
