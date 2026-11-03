package uz.davrbank.compliance.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uz.davrbank.compliance.domain.*
import java.util.UUID

private const val RELATED_COMPLIANCE_IDS_CTE = """
WITH related_compliance_ids AS (
    SELECT es2.compliance_id
    FROM "entity_sanction" es1
    JOIN "entity_sanction" es2
      ON es2.consolidated_sanction_entityguid = es1.consolidated_sanction_entityguid
    WHERE es1.compliance_id = :complianceId

    UNION

    SELECT :complianceId
)
"""

interface ComplianceRepository : JpaRepository<Compliance, String>, JpaSpecificationExecutor<Compliance> {
}

interface EntityDobRepository : JpaRepository<EntityDOB, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<EntityDOB>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceIdIn(complianceIds: Collection<String>): List<EntityDOB>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT ed.*
FROM "entitydob" ed
JOIN related_compliance_ids rci
  ON rci.compliance_id = ed.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<EntityDOB>
}

interface EntityAddressRepository : JpaRepository<EntityAddress, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<EntityAddress>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceIdIn(complianceIds: Collection<String>): List<EntityAddress>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT ea.*
FROM "entity_address" ea
JOIN related_compliance_ids rci
  ON rci.compliance_id = ea.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<EntityAddress>
}

interface EntityAliasRepository : JpaRepository<EntityAlias, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<EntityAlias>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceIdIn(complianceIds: Collection<String>): List<EntityAlias>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT ea.*
FROM "entity_alias" ea
JOIN related_compliance_ids rci
  ON rci.compliance_id = ea.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<EntityAlias>
}

interface EntityIdentificationRepository : JpaRepository<EntityIdentification, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<EntityIdentification>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceIdIn(complianceIds: Collection<String>): List<EntityIdentification>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT ei.*
FROM "entity_identification" ei
JOIN related_compliance_ids rci
  ON rci.compliance_id = ei.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<EntityIdentification>
}

interface EntityCountryAssociationRepository : JpaRepository<EntityCountryAssociation, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<EntityCountryAssociation>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceIdIn(complianceIds: Collection<String>): List<EntityCountryAssociation>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT eca.*
FROM "entity_country_association" eca
JOIN related_compliance_ids rci
  ON rci.compliance_id = eca.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<EntityCountryAssociation>
}

interface EntityPositionRepository : JpaRepository<EntityPosition, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<EntityPosition>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceIdIn(complianceIds: Collection<String>): List<EntityPosition>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT ep.*
FROM "entity_position" ep
JOIN related_compliance_ids rci
  ON rci.compliance_id = ep.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<EntityPosition>
}

interface EntityRelationshipRepository : JpaRepository<EntityRelationship, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<EntityRelationship>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceIdIn(complianceIds: Collection<String>): List<EntityRelationship>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT er.*
FROM "entity_relationship" er
JOIN related_compliance_ids rci
  ON rci.compliance_id = er.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<EntityRelationship>
}

interface EntityRemarkRepository : JpaRepository<EntityRemark, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<EntityRemark>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceIdIn(complianceIds: Collection<String>): List<EntityRemark>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT er.*
FROM "entity_remark" er
JOIN related_compliance_ids rci
  ON rci.compliance_id = er.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<EntityRemark>
}

interface EntityTranslateRepository : JpaRepository<EntityTranslate, UUID> {
    fun findByEntityIdAndEntityType(entityId: UUID, entityType: String): EntityTranslate?

    fun findAllByEntityIdInAndEntityType(entityIds: Collection<UUID>, entityType: String): List<EntityTranslate>
}

interface EntitySourceItemRepository : JpaRepository<EntitySourceItem, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<EntitySourceItem>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceIdIn(complianceIds: Collection<String>): List<EntitySourceItem>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT esi.*
FROM "entity_source_item" esi
JOIN related_compliance_ids rci
  ON rci.compliance_id = esi.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<EntitySourceItem>
}

interface EntitySegmentRepository : JpaRepository<EntitySegment, Long> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findTopByComplianceIdAndDeletedFalseOrderByIdDesc(complianceId: String): EntitySegment?

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceIdInAndDeletedFalse(complianceIds: Collection<String>): List<EntitySegment>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT es.*
FROM "entity_segment" es
JOIN related_compliance_ids rci
  ON rci.compliance_id = es.compliance_id
WHERE es.deleted = false
ORDER BY es.id DESC
LIMIT 1
""",
        nativeQuery = true
    )
    fun findTopRelatedByComplianceIdAndDeletedFalse(@Param("complianceId") complianceId: String): EntitySegment?
}

interface EntityPepRepository : JpaRepository<EntityPEP, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<EntityPEP>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceIdIn(complianceIds: Collection<String>): List<EntityPEP>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT ep.*
FROM "entitypep" ep
JOIN related_compliance_ids rci
  ON rci.compliance_id = ep.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<EntityPEP>
}

interface EntityPepSubcategoryRepository : JpaRepository<EntityPEPSubcategory, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByEntityPepIdIn(entityPepIds: Collection<String>): List<EntityPEPSubcategory>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<EntityPEPSubcategory>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT eps.*
FROM "entitypepsubcategory" eps
JOIN related_compliance_ids rci
  ON rci.compliance_id = eps.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<EntityPEPSubcategory>
}

interface EntityAdverseMediaRepository : JpaRepository<EntityAdverseMedia, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<EntityAdverseMedia>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceIdIn(complianceIds: Collection<String>): List<EntityAdverseMedia>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT eam.*
FROM "entity_adverse_media" eam
JOIN related_compliance_ids rci
  ON rci.compliance_id = eam.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<EntityAdverseMedia>
}

interface EntityAdverseMediaSubcategoryRepository : JpaRepository<EntityAdverseMediaSubcategory, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByEntityAdverseMediaIdIn(entityAdverseMediaIds: Collection<String>): List<EntityAdverseMediaSubcategory>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<EntityAdverseMediaSubcategory>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT eams.*
FROM "entity_adverse_media_subcategory" eams
JOIN related_compliance_ids rci
  ON rci.compliance_id = eams.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<EntityAdverseMediaSubcategory>
}

interface EntityEnforcementRepository : JpaRepository<EntityEnforcement, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<EntityEnforcement>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceIdIn(complianceIds: Collection<String>): List<EntityEnforcement>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT ee.*
FROM "entity_enforcement" ee
JOIN related_compliance_ids rci
  ON rci.compliance_id = ee.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<EntityEnforcement>
}

interface EntityEnforcementSubcategoryRepository : JpaRepository<EntityEnforcementSubcategory, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByEntityEnforcementIdIn(entityEnforcementIds: Collection<String>): List<EntityEnforcementSubcategory>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<EntityEnforcementSubcategory>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT ees.*
FROM "entity_enforcement_subcategory" ees
JOIN related_compliance_ids rci
  ON rci.compliance_id = ees.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<EntityEnforcementSubcategory>
}

interface EntitySanctionRepository : JpaRepository<EntitySanction, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<EntitySanction>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceIdIn(complianceIds: Collection<String>): List<EntitySanction>

    @Query(
        value = """
SELECT DISTINCT es2.compliance_id
FROM "entity_sanction" es1
JOIN "entity_sanction" es2
  ON es2.consolidated_sanction_entityguid = es1.consolidated_sanction_entityguid
WHERE es1.compliance_id = :complianceId
""",
        nativeQuery = true
    )
    fun findRelatedComplianceIds(@Param("complianceId") complianceId: String): List<String>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT es.*
FROM "entity_sanction" es
JOIN related_compliance_ids rci
  ON rci.compliance_id = es.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<EntitySanction>
}

interface EntitySoeRepository : JpaRepository<EntitySOE, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<EntitySOE>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceIdIn(complianceIds: Collection<String>): List<EntitySOE>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT esoe.*
FROM "entitysoe" esoe
JOIN related_compliance_ids rci
  ON rci.compliance_id = esoe.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<EntitySOE>
}

interface EntitySOESubcategoryRepository : JpaRepository<EntitySOESubcategory, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByEntitySOEIdIn(entitySOEIds: Collection<String>): List<EntitySOESubcategory>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<EntitySOESubcategory>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT ess.*
FROM "entitysoesubcategory" ess
JOIN related_compliance_ids rci
  ON rci.compliance_id = ess.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<EntitySOESubcategory>
}

interface EntitySOEDomainRepository : JpaRepository<EntitySOEDomain, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByEntitySOEIdIn(entitySOEIds: Collection<String>): List<EntitySOEDomain>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<EntitySOEDomain>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT esd.*
FROM "entitysoedomain" esd
JOIN related_compliance_ids rci
  ON rci.compliance_id = esd.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<EntitySOEDomain>
}

interface AdditionalSegmentRepository : JpaRepository<AdditionalSegment, Long> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceIdAndDeletedFalse(complianceId: String): List<AdditionalSegment>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceIdInAndDeletedFalse(complianceIds: Collection<String>): List<AdditionalSegment>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT a.*
FROM "additional_segment" a
JOIN related_compliance_ids rci
  ON rci.compliance_id = a.compliance_id
WHERE a.deleted = false
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceIdAndDeletedFalse(
        @Param("complianceId") complianceId: String,
    ): List<AdditionalSegment>
}

interface RecordRepository : JpaRepository<Record, String> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByAdditionalSegmentIdIn(additionalSegmentIds: Collection<Long>): List<Record>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<Record>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT r.*
FROM "record" r
JOIN related_compliance_ids rci
  ON rci.compliance_id = r.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<Record>
}

interface FieldRepository : JpaRepository<Field, Long> {
    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByRecordIdIn(recordIds: Collection<String>): List<Field>

    @EntityGraph(attributePaths = ["compliance"])
    fun findAllByComplianceId(complianceId: String): List<Field>

    @Query(
        value = RELATED_COMPLIANCE_IDS_CTE + """
SELECT f.*
FROM "field" f
JOIN related_compliance_ids rci
  ON rci.compliance_id = f.compliance_id
""",
        nativeQuery = true
    )
    fun findAllRelatedByComplianceId(@Param("complianceId") complianceId: String): List<Field>
}
