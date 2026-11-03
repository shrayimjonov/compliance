package uz.davrbank.compliance.backup

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.davrbank.compliance.ComplianceType
import uz.davrbank.compliance.GeneralApiException
import uz.davrbank.compliance.elasticsearch.ESService
import uz.davrbank.compliance.ifTrue
import kotlin.math.log

interface PGBackupService {
    fun saveEntity(entity: Compliance): Compliance
    fun saveAdditionalData(request: AdditionalDataToEntityDto)
    fun deleteEntity(entityGuid: String)
}


@Service
class PGBackupServiceImpl(
    private val complianceEntityPRepository: ComplianceEntityPRepository,
    private val entityDOBRepository: EntityDOBRepository,
    private val entityCountryAssociationRepository: EntityCountryAssociationRepository,
    private val entityPositionRepository: EntityPositionRepository,
    private val entityRelationshipRepository: EntityRelationshipRepository,
    private val entityRemarkRepository: EntityRemarkRepository,
    private val entityAliasRepository: EntityAliasRepository,
    private val entityIdentificationRepository: EntityIdentificationRepository,
    private val entitySourceItemRepository: EntitySourceItemRepository,
    private val entitySegmentRepository: EntitySegmentRepository,
    private val entityPepRepository: EntityPepRepository,
    private val entityAdverseMediaRepository: EntityAdverseMediaRepository,
    private val entityPepSubCategoryRepository: EntityPepSubCategoryRepository,
    private val entityAdverseMediaSubCategoryRepository: EntityAdverseMediaSubCategoryRepository,
    private val entityAddressRepository: EntityAddressRepository,
    private val entityEnforcementRepository: EntityEnforcementRepository,
    private val entityEnforcementSubcategoryRepository: EntityEnforcementSubcategoryRepository,
    private val entitySanctionRepository: EntitySanctionRepository,
    private val entitySoeRepository: EntitySoeRepository,
    private val additionalSegmentRepository: AdditionalSegmentRepository,
    private val entitySOESubcategoryRepository: EntitySOESubcategoryRepository,
    private val recordRepository: RecordRepository,
    private val entitySOEDomainRepository: EntitySOEDomainRepository,
    private val fieldRepository: FieldRepository,
    private val esService: ESService,
    private val jdbcTemplate: JdbcTemplate,
) : PGBackupService {

    val logger = LoggerFactory.getLogger(this.javaClass)

    override fun saveEntity(entity: Compliance): Compliance {
        return complianceEntityPRepository.save(entity)
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
        val entityPep = mutableListOf<EntityPEP>()
        val entityPEPSubcategory = mutableListOf<EntityPEPSubcategory>()
        val entitySegment = EntitySegment(compliance = request.entityComplianceEntity)

        request.entitySegment.entityPepInternal?.forEach { pep ->
            entityPep.add(pep.entityPep)
            entityPEPSubcategory.add(pep.entityPEPSubcategory)
            entitySegment.entityPep = true
        }

        entityPepRepository.saveAll(entityPep)
        entityPepSubCategoryRepository.saveAll(entityPEPSubcategory)

        val entityAdverseMedia = mutableListOf<EntityAdverseMedia>()
        request.entitySegment.entityAdverseMediaInternalDto?.forEach { adverseMedia ->
            entitySegment.entityAdverseMedia = true
            entityAdverseMedia.add(entityAdverseMediaRepository.save(adverseMedia.entityAdverseMedia))
            entityAdverseMediaSubCategoryRepository.saveAll(adverseMedia.entityAdverseMediaSubcategory)
        }

        val entityEnforcement = mutableListOf<EntityEnforcement>()
        request.entitySegment.entityEnforcementInternalDto?.forEach { enforcementInternal ->
            entitySegment.entityEnforcement = true
            entityEnforcement.add(entityEnforcementRepository.save(enforcementInternal.entityEnforcement))
            entityEnforcementSubcategoryRepository.saveAll(enforcementInternal.entityEnforcementSubcategory)
        }


        request.entitySegment.entitySanctionInternalDto?.map {
            entitySegment.entitySanction = true
            entitySanctionRepository.save(it.entitySanction)
        }

        val entitySoeKeeper = mutableListOf<EntitySOE>()
        request.entitySegment.entitySOEsInternalDto?.forEach { entitySoe ->
            entitySegment.entitySOE = true
            entitySoeKeeper.add(entitySoeRepository.save(entitySoe.entitySOE))
            entitySOEDomainRepository.saveAll(entitySoe.entitySOEDomain)
            entitySOESubcategoryRepository.saveAll(entitySoe.entitySOESubcategory)
        }


        request.entitySegment.additionalSegmentInternalDto?.forEach {
            additionalSegmentRepository.save(it.additionalSegment)
            it.recordInternal.forEach { record ->
                recordRepository.save(record.record)
                fieldRepository.saveAll(record.field)
            }
        }

        entitySegmentRepository.save(entitySegment)


    }

   // @Transactional
    override fun deleteEntity(entityGuid: String) {
        val compliance = complianceEntityPRepository.findByIdOrNull(entityGuid) ?: return
        val complianceId = compliance.id


        val entityRelationships = entityRelationshipRepository.findAllByRelatedEntityGUID(complianceId)
        val allComplianceIds = mutableListOf(complianceId)
        val allEsIds = mutableListOf(entityGuid)

        entityRelationships.forEach { relationship ->
            allComplianceIds.add(relationship.compliance.id)
            allEsIds.add(relationship.id)
        }


        deleteComplianceAndAllRelatedDataBatch(allComplianceIds)


        esService.delete(allEsIds)
    }


    //@Transactional
    fun deleteComplianceAndAllRelatedDataBatch(complianceIds: List<String>) {
        if (complianceIds.isEmpty()) {
            logger.warn("Bo'sh compliance IDs ro'yxati berildi, o'chirish kerak emas.")
            return
        }

        logger.info("${complianceIds.size} ta Compliance va ularga bog'liq ma'lumotlarni batch o'chirish boshlandi.")


        val placeholders = complianceIds.joinToString(",") { "?" }


        val dependentTables = listOf(
            "field",
            "record",
            "entitypepsubcategory",
            "entity_adverse_media_subcategory",
            "entity_enforcement_subcategory",
            "entitysoesubcategory",
            "entitysoedomain",

            // To'g'ridan-to'g'ri Compliance'ga bog'liq jadvallar
            "additional_segment",
            "entitypep",
            "entity_adverse_media",
            "entity_enforcement",
            "entitysoe",
            "entitydob",
            "entity_address",
            "entity_alias",
            "entity_identification",
            "entity_country_association",
            "entity_position",
            "entity_relationship",
            "entity_remark",
            "entity_source_item",
            "entity_sanction",
            "entity_segment"
        )

        var totalDeletedRows = 0

        dependentTables.forEach { tableName ->
            val sql = "DELETE FROM $tableName WHERE compliance_id IN ($placeholders)"
            try {
                val deletedRows = jdbcTemplate.update(sql, *complianceIds.toTypedArray())
                if (deletedRows > 0) {
                    totalDeletedRows += deletedRows
                    logger.debug("'$tableName' jadvalidan $deletedRows ta yozuv o'chirildi.")
                }
            } catch (e: Exception) {
                logger.error("'$tableName' jadvalidan batch o'chirishda xatolik: ${e.message}", e)
            }
        }


        logger.info("Barcha bog'liq yozuvlar o'chirildi ($totalDeletedRows ta). Asosiy compliance yozuvlari o'chirilmoqda...${placeholders}")
        val deleteComplianceSql = "DELETE FROM compliance WHERE id IN ($placeholders)"
        val finalDeletedRows = jdbcTemplate.update(deleteComplianceSql, *complianceIds.toTypedArray())

        logger.info("$finalDeletedRows ta Compliance va jami ${totalDeletedRows + finalDeletedRows} ta yozuv muvaffaqiyatli o'chirildi.")
    }

}