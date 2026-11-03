package uz.davrbank.compliance.dbconsumer.service.impl

import mu.KotlinLogging
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.davrbank.compliance.dbconsumer.domain.postgresql.Compliance
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityPEP
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityPEPSubcategory
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntitySegment
import uz.davrbank.compliance.dbconsumer.dto.internal.AdditionalDataToEntityDto
import uz.davrbank.compliance.dbconsumer.repository.postgresql.*
import uz.davrbank.compliance.dbconsumer.service.ElasticsearchWriterService
import uz.davrbank.compliance.dbconsumer.service.PostgresWriterService

@Service
class PostgresWriterServiceImpl(
    private val complianceRepository: ComplianceRepository,
    private val entityDOBRepository: EntityDOBRepository,
    private val entityCountryAssociationRepository: EntityCountryAssociationRepository,
    private val entityPositionRepository: EntityPositionRepository,
    private val entityRelationshipRepository: EntityRelationshipRepository,
    private val entityRemarkRepository: EntityRemarkRepository,
    private val entityAliasRepository: EntityAliasRepository,
    private val entityIdentificationRepository: EntityIdentificationRepository,
    private val entitySourceItemRepository: EntitySourceItemRepository,
    private val entitySegmentRepository: EntitySegmentRepository,
    private val entityPEPRepository: EntityPEPRepository,
    private val entityPEPSubcategoryRepository: EntityPEPSubcategoryRepository,
    private val entityAdverseMediaRepository: EntityAdverseMediaRepository,
    private val entityAdverseMediaSubcategoryRepository: EntityAdverseMediaSubcategoryRepository,
    private val entityAddressRepository: EntityAddressRepository,
    private val entityEnforcementRepository: EntityEnforcementRepository,
    private val entityEnforcementSubcategoryRepository: EntityEnforcementSubcategoryRepository,
    private val entitySanctionRepository: EntitySanctionRepository,
    private val entitySOERepository: EntitySOERepository,
    private val additionalSegmentRepository: AdditionalSegmentRepository,
    private val entitySOESubcategoryRepository: EntitySOESubcategoryRepository,
    private val recordRepository: RecordRepository,
    private val entitySOEDomainRepository: EntitySOEDomainRepository,
    private val fieldRepository: FieldRepository,
    private val elasticsearchWriterService: ElasticsearchWriterService,
    private val jdbcTemplate: JdbcTemplate,
) : PostgresWriterService {

    private val logger = KotlinLogging.logger {}

    override fun saveEntity(entity: Compliance): Compliance {
        return complianceRepository.save(entity)
    }

    @Transactional
    override fun saveAdditionalData(request: AdditionalDataToEntityDto) {
        entityDOBRepository.saveAll(request.entityDOBs)
        entityCountryAssociationRepository.saveAll(request.entityCountryAssociations)
        entityPositionRepository.saveAll(request.entityPositions)
        entityRelationshipRepository.saveAll(request.entityRelationships)
        entityRemarkRepository.saveAll(request.entityRemarks)
        entityAliasRepository.saveAll(request.entityAlias)
        entityIdentificationRepository.saveAll(request.entityIdentification)
        entitySourceItemRepository.saveAll(request.entitySourceItem)
        entityAddressRepository.saveAll(request.entityAddresses)

        val entitySegment = entitySegmentRepository
            .findFirstByComplianceIdOrderByIdDesc(request.entityComplianceEntity.id)
            ?: EntitySegment(compliance = request.entityComplianceEntity)

        val peps = mutableListOf<EntityPEP>()
        val pepSubcategories = mutableListOf<EntityPEPSubcategory>()
        request.entitySegment.entityPepInternal?.forEach { pep ->
            peps.add(pep.entityPep)
            pepSubcategories.add(pep.entityPEPSubcategory)
            entitySegment.entityPep = true
        }
        entityPEPRepository.saveAll(peps)
        entityPEPSubcategoryRepository.saveAll(pepSubcategories)

        request.entitySegment.entityAdverseMediaInternalDto?.forEach { adverseMedia ->
            entitySegment.entityAdverseMedia = true
            entityAdverseMediaRepository.save(adverseMedia.entityAdverseMedia)
            entityAdverseMediaSubcategoryRepository.saveAll(adverseMedia.entityAdverseMediaSubcategory)
        }

        request.entitySegment.entityEnforcementInternalDto?.forEach { enforcementInternal ->
            entitySegment.entityEnforcement = true
            entityEnforcementRepository.save(enforcementInternal.entityEnforcement)
            entityEnforcementSubcategoryRepository.saveAll(enforcementInternal.entityEnforcementSubcategory)
        }

        request.entitySegment.entitySanctionInternalDto?.forEach {
            entitySegment.entitySanction = true
            entitySanctionRepository.save(it.entitySanction)
        }

        request.entitySegment.entitySOEsInternalDto?.forEach { entitySoe ->
            entitySegment.entitySOE = true
            entitySOERepository.save(entitySoe.entitySOE)
            entitySOEDomainRepository.saveAll(entitySoe.entitySOEDomain)
            entitySOESubcategoryRepository.saveAll(entitySoe.entitySOESubcategory)
        }

        request.entitySegment.additionalSegmentInternalDto?.forEach {
            additionalSegmentRepository.save(it.additionalSegment)
            it.recordInternal.forEach { recordDto ->
                recordRepository.save(recordDto.record)
                fieldRepository.saveAll(recordDto.field)
            }
        }

        entitySegmentRepository.save(entitySegment)
    }

    override fun deleteEntity(entityGuid: String) {
        val compliance = complianceRepository.findById(entityGuid).orElse(null)
        if (compliance == null) {
            logger.warn { "Deletion skipped: Compliance entity not found for GUID: $entityGuid" }
            return
        }

        val complianceId = compliance.id
        val entityRelationships = entityRelationshipRepository.findAllByRelatedEntityGUID(complianceId)
        val allComplianceIds = mutableListOf(complianceId)
        val allEsIds = mutableListOf(entityGuid)

        entityRelationships.forEach { relationship ->
            allComplianceIds.add(relationship.compliance.id)
            allEsIds.add(relationship.id)
        }

        deleteComplianceAndAllRelatedDataBatch(allComplianceIds)
        elasticsearchWriterService.delete(allEsIds)
    }

    private fun deleteComplianceAndAllRelatedDataBatch(complianceIds: List<String>) {
        if (complianceIds.isEmpty()) {
            logger.warn { "No compliance IDs provided for batch deletion." }
            return
        }
        logger.info { "Starting batch deletion for ${complianceIds.size} compliance records." }

        val placeholders = complianceIds.joinToString(",") { "?" }
        val dependentTables = listOf(
            "field", "record",
            "entitypepsubcategory", "entity_adverse_media_subcategory",
            "entity_enforcement_subcategory", "entitysoesubcategory", "entitysoedomain",
            "additional_segment", "entitypep", "entity_adverse_media", "entity_enforcement",
            "entitysoe", "entitydob", "entity_address", "entity_alias", "entity_identification",
            "entity_country_association", "entity_position", "entity_relationship",
            "entity_remark", "entity_source_item", "entity_sanction", "entity_segment"
        )

        var totalDeleted = 0
        dependentTables.forEach { tableName ->
            try {
                val deletedCount = jdbcTemplate.update(
                    "DELETE FROM $tableName WHERE compliance_id IN ($placeholders)",
                    *complianceIds.toTypedArray()
                )
                if (deletedCount > 0) {
                    totalDeleted += deletedCount
                    logger.debug { "Successfully deleted $deletedCount records from table '$tableName'." }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to delete records from table '$tableName': ${e.message}" }
            }
        }

        val finalComplianceDeleted = jdbcTemplate.update(
            "DELETE FROM compliance WHERE id IN ($placeholders)",
            *complianceIds.toTypedArray()
        )

        logger.info {
            "Batch deletion completed. Deleted $finalComplianceDeleted Compliance entries and $totalDeleted related records."
        }
    }
}