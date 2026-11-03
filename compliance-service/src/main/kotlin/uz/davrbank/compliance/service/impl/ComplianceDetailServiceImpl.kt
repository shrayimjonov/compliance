package uz.davrbank.compliance.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.davrbank.compliance.domain.*
import uz.davrbank.compliance.dto.response.*
import uz.davrbank.compliance.enums.EntityType
import uz.davrbank.compliance.exception.NotFoundException
import uz.davrbank.compliance.repository.*
import uz.davrbank.compliance.service.ComplianceDetailService
import java.util.*

@Service
@Transactional(readOnly = true)
class ComplianceDetailServiceImpl(
    private val complianceRepository: ComplianceRepository,
    private val entityDobRepository: EntityDobRepository,
    private val entityAddressRepository: EntityAddressRepository,
    private val entityAliasRepository: EntityAliasRepository,
    private val entityIdentificationRepository: EntityIdentificationRepository,
    private val entityCountryAssociationRepository: EntityCountryAssociationRepository,
    private val entityPositionRepository: EntityPositionRepository,
    private val entityRelationshipRepository: EntityRelationshipRepository,
    private val entityRemarkRepository: EntityRemarkRepository,
    private val entityTranslateRepository: EntityTranslateRepository,
    private val entitySourceItemRepository: EntitySourceItemRepository,
    private val entitySegmentRepository: EntitySegmentRepository,
    private val entityPepRepository: EntityPepRepository,
    private val entityPepSubcategoryRepository: EntityPepSubcategoryRepository,
    private val entityAdverseMediaRepository: EntityAdverseMediaRepository,
    private val entityAdverseMediaSubcategoryRepository: EntityAdverseMediaSubcategoryRepository,
    private val entityEnforcementRepository: EntityEnforcementRepository,
    private val entityEnforcementSubcategoryRepository: EntityEnforcementSubcategoryRepository,
    private val entitySanctionRepository: EntitySanctionRepository,
    private val entitySoeRepository: EntitySoeRepository,
    private val entitySOESubcategoryRepository: EntitySOESubcategoryRepository,
    private val entitySOEDomainRepository: EntitySOEDomainRepository,
    private val additionalSegmentRepository: AdditionalSegmentRepository,
    private val recordRepository: RecordRepository,
    private val fieldRepository: FieldRepository,
) : ComplianceDetailService {

    override fun getComplianceDetails(complianceId: String): ComplianceDetailsDto {
        val compliance = findCompliance(complianceId)
        val relatedComplianceIds = relatedComplianceIds(complianceId)

        val entityDobs = entityDobRepository.findAllByComplianceIdIn(relatedComplianceIds).sortedBy { it.id }
        val entityAddresses = entityAddressRepository.findAllByComplianceIdIn(relatedComplianceIds).sortedBy { it.id }
        val entityAliases = entityAliasRepository.findAllByComplianceIdIn(relatedComplianceIds).sortedBy { it.id }
        val entityIdentifications =
            entityIdentificationRepository.findAllByComplianceIdIn(relatedComplianceIds).sortedBy { it.id }
        val entityCountryAssociations =
            entityCountryAssociationRepository.findAllByComplianceIdIn(relatedComplianceIds).sortedBy { it.id }
        val entityPositions = entityPositionRepository.findAllByComplianceIdIn(relatedComplianceIds).sortedBy { it.id }
        val entityRelationships = entityRelationshipRepository.findAllByComplianceIdIn(relatedComplianceIds).sortedBy { it.id }
        val entityRemarks = entityRemarkRepository.findAllByComplianceIdIn(relatedComplianceIds).sortedBy { it.id }
        val entityRemarkTranslations = fetchRemarkTranslations(entityRemarks)
        val entitySourceItems = entitySourceItemRepository.findAllByComplianceIdIn(relatedComplianceIds).sortedBy { it.id }
        val entitySegment = entitySegmentRepository.findAllByComplianceIdInAndDeletedFalse(relatedComplianceIds)
            .maxByOrNull { it.id ?: Long.MIN_VALUE }
        val entityPeps = entityPepRepository.findAllByComplianceIdIn(relatedComplianceIds).sortedBy { it.id }
        val entityPepSubcategories = fetchPepSubcategories(entityPeps)
        val entityAdverseMedia = entityAdverseMediaRepository.findAllByComplianceIdIn(relatedComplianceIds)
            .sortedBy { it.id }
        val entityAdverseMediaSubcategories = fetchAdverseMediaSubcategories(entityAdverseMedia)
        val entityEnforcements = entityEnforcementRepository.findAllByComplianceIdIn(relatedComplianceIds)
            .sortedBy { it.id }
        val entityEnforcementSubcategories = fetchEnforcementSubcategories(entityEnforcements)
        val entitySanctions = entitySanctionRepository.findAllByComplianceIdIn(relatedComplianceIds).sortedBy { it.id }
        val entitySoes = entitySoeRepository.findAllByComplianceIdIn(relatedComplianceIds).sortedBy { it.id }
        val entitySoeSubcategories = fetchSoeSubcategories(entitySoes)
        val entitySoeDomains = fetchSoeDomains(entitySoes)
        val additionalSegments = additionalSegmentRepository.findAllByComplianceIdInAndDeletedFalse(relatedComplianceIds)
            .sortedBy { it.id }
        val records = fetchRecords(additionalSegments)
        val fields = fetchFields(records)

        return ComplianceDetailsDto(
            compliance = compliance.toDetailDto(),
            entityDobs = entityDobs.map { it.toDetailDto() },
            entityAddresses = entityAddresses.map { it.toDetailDto() },
            entityAliases = entityAliases.map { it.toDetailDto() },
            entityIdentifications = entityIdentifications.map { it.toDetailDto() },
            entityCountryAssociations = entityCountryAssociations.map { it.toDetailDto() },
            entityPositions = entityPositions.map { it.toDetailDto() },
            entityRelationships = entityRelationships.toRelationshipDetailDtos(),
            entityRemarks = entityRemarks.map { it.toDetailDto(entityRemarkTranslations[it.id]) },
            entitySourceItems = entitySourceItems.map { it.toDetailDto() },
            entitySegment = entitySegment?.toDetailDto(),
            entityPeps = entityPeps.map { it.toDetailDto() },
            entityPepSubcategories = entityPepSubcategories.map { it.toDetailDto() },
            entityAdverseMedia = entityAdverseMedia.map { it.toDetailDto() },
            entityAdverseMediaSubcategories = entityAdverseMediaSubcategories.map { it.toDetailDto() },
            entityEnforcements = entityEnforcements.map { it.toDetailDto() },
            entityEnforcementSubcategories = entityEnforcementSubcategories.map { it.toDetailDto() },
            entitySanctions = entitySanctions.map { it.toDetailDto() },
            entitySoes = entitySoes.map { it.toDetailDto() },
            entitySoeSubcategories = entitySoeSubcategories.map { it.toDetailDto() },
            entitySoeDomains = entitySoeDomains.map { it.toDetailDto() },
            additionalSegments = additionalSegments.map { it.toDetailDto() },
            records = records.map { it.toDetailDto() },
            fields = fields.map { it.toDetailDto() },
        )
    }

    override fun getCompliance(complianceId: String): ComplianceDetailDto =
        findCompliance(complianceId).toDetailDto()

    override fun getEntityDobs(complianceId: String): List<EntityDobDetailDto> =
        forExistingCompliance(complianceId) {
            entityDobRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    override fun getEntityAddresses(complianceId: String): List<EntityAddressDetailDto> =
        forExistingCompliance(complianceId) {
            entityAddressRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    override fun getEntityAliases(complianceId: String): List<EntityAliasDetailDto> =
        forExistingCompliance(complianceId) {
            entityAliasRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    override fun getEntityIdentifications(complianceId: String): List<EntityIdentificationDetailDto> =
        forExistingCompliance(complianceId) {
            entityIdentificationRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    override fun getEntityCountryAssociations(complianceId: String): List<EntityCountryAssociationDetailDto> =
        forExistingCompliance(complianceId) {
            entityCountryAssociationRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    override fun getEntityPositions(complianceId: String): List<EntityPositionDetailDto> =
        forExistingCompliance(complianceId) {
            entityPositionRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    override fun getEntityRelationships(complianceId: String): List<EntityRelationshipDetailDto> =
        forExistingCompliance(complianceId) {
            entityRelationshipRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .toRelationshipDetailDtos()
        }

    override fun getEntityRemarks(complianceId: String): List<EntityRemarkDetailDto> =
        forExistingCompliance(complianceId) {
            val entityRemarks = entityRemarkRepository.findAllRelatedByComplianceId(complianceId).sortedBy { it.id }
            val translations = fetchRemarkTranslations(entityRemarks)

            entityRemarks.map { entityRemark -> entityRemark.toDetailDto(translations[entityRemark.id]) }
        }

    override fun getEntitySourceItems(complianceId: String): List<EntitySourceItemDetailDto> =
        forExistingCompliance(complianceId) {
            entitySourceItemRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    override fun getEntitySegment(complianceId: String): EntitySegmentDetailDto? =
        forExistingCompliance(complianceId) {
            entitySegmentRepository.findTopRelatedByComplianceIdAndDeletedFalse(complianceId)
                ?.toDetailDto()
        }

    override fun getEntityPeps(complianceId: String): List<EntityPepDetailDto> =
        forExistingCompliance(complianceId) {
            entityPepRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    override fun getEntityPepSubcategories(complianceId: String): List<EntityPepSubcategoryDetailDto> =
        forExistingCompliance(complianceId) {
            entityPepSubcategoryRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    override fun getEntityAdverseMedia(complianceId: String): List<EntityAdverseMediaDetailDto> =
        forExistingCompliance(complianceId) {
            entityAdverseMediaRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    override fun getEntityAdverseMediaSubcategories(
        complianceId: String,
    ): List<EntityAdverseMediaSubcategoryDetailDto> =
        forExistingCompliance(complianceId) {
            entityAdverseMediaSubcategoryRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    override fun getEntityEnforcements(complianceId: String): List<EntityEnforcementDetailDto> =
        forExistingCompliance(complianceId) {
            entityEnforcementRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    override fun getEntityEnforcementSubcategories(complianceId: String): List<EntityEnforcementSubcategoryDetailDto> =
        forExistingCompliance(complianceId) {
            entityEnforcementSubcategoryRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    override fun getEntitySanctions(complianceId: String): List<EntitySanctionDetailDto> =
        forExistingCompliance(complianceId) {
            entitySanctionRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    override fun getEntitySoes(complianceId: String): List<EntitySoeDetailDto> =
        forExistingCompliance(complianceId) {
            entitySoeRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    override fun getEntitySoeSubcategories(complianceId: String): List<EntitySoeSubcategoryDetailDto> =
        forExistingCompliance(complianceId) {
            entitySOESubcategoryRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    override fun getEntitySoeDomains(complianceId: String): List<EntitySoeDomainDetailDto> =
        forExistingCompliance(complianceId) {
            entitySOEDomainRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    override fun getAdditionalSegments(complianceId: String): List<AdditionalSegmentDetailDto> =
        forExistingCompliance(complianceId) {
            additionalSegmentRepository.findAllRelatedByComplianceIdAndDeletedFalse(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    override fun getRecords(complianceId: String): List<RecordDetailDto> =
        forExistingCompliance(complianceId) {
            recordRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    override fun getFields(complianceId: String): List<FieldDetailDto> =
        forExistingCompliance(complianceId) {
            fieldRepository.findAllRelatedByComplianceId(complianceId)
                .sortedBy { it.id }
                .map { it.toDetailDto() }
        }

    private fun <T> forExistingCompliance(complianceId: String, fetch: () -> T): T {
        requireComplianceExists(complianceId)
        return fetch()
    }

    private fun requireComplianceExists(complianceId: String) {
        if (!complianceRepository.existsById(complianceId)) {
            throw complianceNotFound(complianceId)
        }
    }

    private fun findCompliance(complianceId: String) =
        complianceRepository.findByIdOrNull(complianceId) ?: throw complianceNotFound(complianceId)

    private fun relatedComplianceIds(complianceId: String): List<String> =
        (entitySanctionRepository.findRelatedComplianceIds(complianceId) + complianceId)
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()

    private fun complianceNotFound(complianceId: String) =
        NotFoundException("Compliance entity not found for id=$complianceId")

    private fun List<EntityRelationship>.toRelationshipDetailDtos(): List<EntityRelationshipDetailDto> {
        val relatedEntityById = fetchRelatedEntities(this)
        return map { relationship ->
            relationship.toDetailDto(relatedEntityById[relationship.relatedEntityGUID])
        }.sortedWith(
            compareBy<EntityRelationshipDetailDto> { relationshipGroupSortOrder(it.groupDesc) }
                .thenBy { it.id },
        )
    }

    private fun relationshipGroupSortOrder(groupDesc: String?) = when (groupDesc) {
        "Family" -> 0
        "Association" -> 1
        "Company" -> 2
        else -> Int.MAX_VALUE
    }

    private fun fetchRelatedEntities(entityRelationships: List<EntityRelationship>): Map<String, Compliance> {
        val relatedEntityIds = entityRelationships
            .mapNotNull { it.relatedEntityGUID?.takeIf(String::isNotBlank) }
            .distinct()

        if (relatedEntityIds.isEmpty()) {
            return emptyMap()
        }

        return complianceRepository.findAllById(relatedEntityIds).associateBy { it.id }
    }

    private fun fetchPepSubcategories(entityPeps: List<EntityPEP>): List<EntityPEPSubcategory> {
        val pepIds = entityPeps.map { it.id }
        if (pepIds.isEmpty()) {
            return emptyList()
        }

        return entityPepSubcategoryRepository.findAllByEntityPepIdIn(pepIds).sortedBy { it.id }
    }

    private fun fetchAdverseMediaSubcategories(
        entityAdverseMedia: List<EntityAdverseMedia>,
    ): List<EntityAdverseMediaSubcategory> {
        val adverseMediaIds = entityAdverseMedia.map { it.id }
        if (adverseMediaIds.isEmpty()) {
            return emptyList()
        }

        return entityAdverseMediaSubcategoryRepository.findAllByEntityAdverseMediaIdIn(adverseMediaIds)
            .sortedBy { it.id }
    }

    private fun fetchEnforcementSubcategories(
        entityEnforcements: List<EntityEnforcement>,
    ): List<EntityEnforcementSubcategory> {
        val enforcementIds = entityEnforcements.map { it.id }
        if (enforcementIds.isEmpty()) {
            return emptyList()
        }

        return entityEnforcementSubcategoryRepository.findAllByEntityEnforcementIdIn(enforcementIds)
            .sortedBy { it.id }
    }

    private fun fetchSoeSubcategories(entitySoes: List<EntitySOE>): List<EntitySOESubcategory> {
        val soeIds = entitySoes.map { it.id }
        if (soeIds.isEmpty()) {
            return emptyList()
        }

        return entitySOESubcategoryRepository.findAllByEntitySOEIdIn(soeIds).sortedBy { it.id }
    }

    private fun fetchSoeDomains(entitySoes: List<EntitySOE>): List<EntitySOEDomain> {
        val soeIds = entitySoes.map { it.id }
        if (soeIds.isEmpty()) {
            return emptyList()
        }

        return entitySOEDomainRepository.findAllByEntitySOEIdIn(soeIds).sortedBy { it.id }
    }

    private fun fetchRecords(additionalSegments: List<AdditionalSegment>): List<Record> {
        val additionalSegmentIds = additionalSegments.mapNotNull { it.id }
        if (additionalSegmentIds.isEmpty()) {
            return emptyList()
        }

        return recordRepository.findAllByAdditionalSegmentIdIn(additionalSegmentIds).sortedBy { it.id }
    }

    private fun fetchFields(records: List<Record>) =
        records.map { it.id }
            .takeIf { it.isNotEmpty() }
            ?.let(fieldRepository::findAllByRecordIdIn)
            ?.sortedBy { it.id }
            ?: emptyList()

    private fun fetchRemarkTranslations(entityRemarks: List<EntityRemark>): Map<String, EntityTranslate> {
        val remarkIdByUuid = entityRemarks.mapNotNull { entityRemark ->
            entityRemark.id.toUuidOrNull()?.let { uuid -> uuid to entityRemark.id }
        }.toMap()
        val remarkIds = remarkIdByUuid.keys
        if (remarkIds.isEmpty()) {
            return emptyMap()
        }

        return entityTranslateRepository.findAllByEntityIdInAndEntityType(remarkIds, ENTITY_REMARK_TYPE)
            .mapNotNull { entityTranslate ->
                remarkIdByUuid[entityTranslate.entityId]?.let { remarkId -> remarkId to entityTranslate }
            }
            .toMap()
    }

    private fun String.toUuidOrNull(): UUID? =
        runCatching { UUID.fromString(this) }.getOrNull()

    companion object {
        private val ENTITY_REMARK_TYPE = EntityType.ENTITY_REMARK.value
    }
}
