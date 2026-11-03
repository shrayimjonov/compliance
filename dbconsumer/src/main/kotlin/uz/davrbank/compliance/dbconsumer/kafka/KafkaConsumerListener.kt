package uz.davrbank.compliance.dbconsumer.kafka

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import mu.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import uz.davrbank.compliance.dbconsumer.dto.kafka.ComplianceEntityDto
import uz.davrbank.compliance.dbconsumer.dto.kafka.DeleteEventKafka
import uz.davrbank.compliance.dbconsumer.enums.ComplianceType
import uz.davrbank.compliance.dbconsumer.service.DeleteEntityService
import uz.davrbank.compliance.dbconsumer.service.ElasticsearchWriterService
import uz.davrbank.compliance.dbconsumer.service.PostgresWriterService
import uz.davrbank.compliance.dbconsumer.service.impl.EntityDataExtractorService

@Component
class KafkaConsumerListener(
    private val elasticsearchWriterService: ElasticsearchWriterService,
    private val deleteEntityService: DeleteEntityService,
    private val entityDataExtractorService: EntityDataExtractorService,
    private val postgresWriterService: PostgresWriterService,
    private val xmlMapper: XmlMapper
    ) {
    private val logger = KotlinLogging.logger {}

    @KafkaListener(
        id = "compliance-entity-consumer",
        topics = [ENTITY, RELATIONSHIP],
        concurrency = "10"
    )
    fun mainEntityListener(message: String) {
        try {
            logger.debug { "Received message length: ${message.length}" }

            if (message.contains("<EntityDelete>")) {
                val deleteDto = xmlMapper.readValue(message, DeleteEventKafka::class.java)
                deleteEntityService.saveDeleteEvent(deleteDto)
                logger.info { "Entity deleted successfully: ${deleteDto.EntityGUID}" }
                return
            }

            entityAndRelationship(message)

        } catch (e: Exception) {
            logger.error(e) { "Error processing Kafka message. Raw payload: $message" }
            throw e
        }
    }

    @KafkaListener(
        id = "compliance-relationship-consumer",
        topics = [RELATIONSHIP],
        concurrency = "10"
    )
    fun mainRelationshipListener(message: String) {
        try {
            logger.debug { "Received message length: ${message.length}" }
            entityAndRelationship(message)
        } catch (e: Exception) {
            logger.error(e) { "Error processing Kafka message. Raw payload: $message" }
            throw e
        }
    }

    @KafkaListener(
        id = "compliance-delete-direct-consumer",
        topics = [ENTITY_DELETE]
    )
    fun directDeleteListener(message: String) {
        try {
            val dto = xmlMapper.readValue(message, DeleteEventKafka::class.java)

            deleteEntityService.saveDeleteEvent(dto)
            logger.info { "Delete event processed for GUID: ${dto.EntityGUID}" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to process delete event message: $message" }
            throw e
        }
    }

    private fun entityAndRelationship(message: String = "") {
        val sanitized = message.replace(Regex("&(?!amp;|lt;|gt;|quot;|apos;)"), "&amp;")
        val complianceType =
            if (message.contains("<Relationship>")) ComplianceType.RELATIONSHIP else ComplianceType.ENTITY
        val dto = xmlMapper.readValue(sanitized, ComplianceEntityDto::class.java)
        processEntity(dto, complianceType)
    }

    private fun processEntity(dto: ComplianceEntityDto, complianceType: ComplianceType) {
        val startTime = System.currentTimeMillis()
        logger.info { "Starting processing for ${complianceType.name}: ${dto.entityGuid}" }

        try {
            val (complianceEntityDoc, complianceEntity) = entityDataExtractorService.toComplianceModels(
                dto,
                complianceType
            )

            val savedEntity = postgresWriterService.saveEntity(complianceEntity)

            elasticsearchWriterService.save(complianceEntityDoc)

            val additionalData = entityDataExtractorService.extractAdditionalData(dto, savedEntity, complianceType)
            postgresWriterService.saveAdditionalData(additionalData)

            logger.debug { "Successfully processed ${complianceType.name} [${dto.entityGuid}] in ${System.currentTimeMillis() - startTime}ms" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to process entity for GUID: ${dto.entityGuid}. ComplianceType: $complianceType" }
            throw e
        }
    }
}