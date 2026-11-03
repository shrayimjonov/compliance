package uz.davrbank.compliance.backup


import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uz.davrbank.compliance.*

@Repository
interface ComplianceEntityPRepository : JpaRepository<Compliance, String>  {
    fun findAllByDataType(dataType: DataType, pageable: Pageable): Page<Compliance>
    fun findAllByIdIn(ids: List<String>): List<Compliance>

    @Query("select e from Compliance e order by e.createdDate asc")
    fun findAllOrderByCreatedDate(pageable: Pageable): Page<Compliance>

}

@Repository
interface EntityDOBRepository : JpaRepository<EntityDOB, String>  {
    fun findAllByCompliance(entity: Compliance): MutableList<EntityDOB>

}

@Repository
interface EntityAddressRepository : JpaRepository<EntityAddress, String>  {
    fun findAllByCompliance(entity: Compliance): MutableList<EntityAddress>

}

@Repository
interface EntityCountryAssociationRepository : JpaRepository<EntityCountryAssociation, String>  {
    fun findAllByCompliance(entity: Compliance): MutableList<EntityCountryAssociation>
}

@Repository
interface EntityPositionRepository : JpaRepository<EntityPosition, String>  {
    fun findAllByCompliance(entity: Compliance): MutableList<EntityPosition>
}

@Repository
interface EntityRelationshipRepository : JpaRepository<EntityRelationship, String>  {
    fun findAllById(rel: String): List<EntityRelationship>

    fun findAllByComplianceTypeAndId(
        type: ComplianceType,
        rel: String
    ): List<EntityRelationship>



    fun findAllByComplianceTypeAndRelatedEntityGUID(type: ComplianceType, rel: String): List<EntityRelationship>

    fun findAllByCompliance(entity: Compliance): MutableList<EntityRelationship>
    fun findAllByRelatedEntityGUID(rel: String): List<EntityRelationship>
}

@Repository
interface EntityRemarkRepository : JpaRepository<EntityRemark, String>  {
    fun findAllByCompliance(entity: Compliance): MutableList<EntityRemark>
}

@Repository
interface EntityAliasRepository : JpaRepository<EntityAlias, String>  {
    fun findAllByCompliance(entity: Compliance): MutableList<EntityAlias>
}

@Repository
interface EntityIdentificationRepository :JpaRepository<EntityIdentification, String>  {
    fun findAllByCompliance(entity: Compliance): MutableList<EntityIdentification>
}

@Repository
interface EntitySourceItemRepository : JpaRepository<EntitySourceItem, String>  {
    fun findAllByCompliance(entity: Compliance): MutableList<EntitySourceItem>
}

//@Repository
//interface EntitySegmentRepository : BaseRepository<EntitySegment> {
//
//}

@Repository
interface EntityPepRepository : JpaRepository<EntityPEP, String>  {
    fun findAllByCompliance(entity: Compliance): MutableList<EntityPEP>
}

@Repository
interface EntityAdverseMediaRepository : JpaRepository<EntityAdverseMedia, String>  {
    fun findAllByCompliance(entity: Compliance): MutableList<EntityAdverseMedia>
}

@Repository
interface EntityPepSubCategoryRepository : JpaRepository<EntityPEPSubcategory, String>  {
    fun findAllByCompliance(entity: Compliance): MutableList<EntityPEPSubcategory>
    fun findAllByEntityPep(pep: EntityPEP): MutableList<EntityPEPSubcategory>
    fun findAllByEntityPepIdIn(entityPepIds: List<String>): MutableList<EntityPEPSubcategory>
}

@Repository
interface EntityAdverseMediaSubCategoryRepository : JpaRepository<EntityAdverseMediaSubcategory, String>  {
    fun findAllByCompliance(entity: Compliance): MutableList<EntityAdverseMediaSubcategory>
    fun findAllByEntityAdverseMedia(entity: EntityAdverseMedia): MutableList<EntityAdverseMediaSubcategory>
    fun findAllByEntityAdverseMediaIdIn(entityAdverseMediaIds: List<String>): MutableList<EntityAdverseMediaSubcategory>
}

@Repository
interface EntityEnforcementRepository : JpaRepository<EntityEnforcement, String>  {
    fun findAllByCompliance(entity: Compliance): MutableList<EntityEnforcement>
}

@Repository
interface EntityEnforcementSubcategoryRepository : JpaRepository<EntityEnforcementSubcategory, String>  {
    fun findAllByCompliance(entity: Compliance): MutableList<EntityEnforcementSubcategory>
    fun findAllByEntityEnforcement(entity: EntityEnforcement): MutableList<EntityEnforcementSubcategory>
    fun findAllByEntityEnforcementIdIn(entityEnforcementIds: List<String>): MutableList<EntityEnforcementSubcategory>

}

@Repository
interface EntitySanctionRepository : JpaRepository<EntitySanction, String>  {
    fun findAllByCompliance(entity: Compliance): MutableList<EntitySanction>
}

@Repository
interface EntitySoeRepository : JpaRepository<EntitySOE, String> {
    fun findAllByCompliance(entity: Compliance): MutableList<EntitySOE>

}

@Repository
interface EntitySOESubcategoryRepository : JpaRepository<EntitySOESubcategory, String> {
    fun findAllByCompliance(compliance: Compliance): MutableList<EntitySOESubcategory>
    fun findAllByEntitySOE(entity: EntitySOE): MutableList<EntitySOESubcategory>
    fun findAllByEntitySOEIdIn(entitySOEIDs: List<String>): MutableList<EntitySOESubcategory>
}

@Repository
interface EntitySOEDomainRepository : JpaRepository<EntitySOEDomain, String> {
    fun findAllByCompliance(compliance: Compliance): MutableList<EntitySOEDomain>
    fun findAllByEntitySOE(entity: EntitySOE): MutableList<EntitySOEDomain>
    fun findAllByEntitySOEIdIn(entitySOEIDs: List<String>): MutableList<EntitySOEDomain>
}

@Repository
interface AdditionalSegmentRepository : JpaRepository<AdditionalSegment, String> {
    fun findAllByComplianceAndDeletedFalse(entity: Compliance): MutableList<AdditionalSegment>
}

@Repository
interface RecordRepository : JpaRepository<Record, String> {
    fun findAllByCompliance(entity: Compliance): MutableList<Record>
    fun findAllByAdditionalSegment(entity: AdditionalSegment): MutableList<Record>
}

@Repository
interface FieldRepository : JpaRepository<Field, String> {
    fun findAllByComplianceAndDeletedFalse(entity: Compliance): MutableList<Field>
    fun findAllByRecord(record: Record): MutableList<Field>
}

@Repository
interface EntitySegmentRepository : JpaRepository<EntitySegment, String> {
    fun findByComplianceAndDeletedFalse(compliance: Compliance): EntitySegment?
    fun findAllByComplianceAndDeletedFalse(entity: Compliance): MutableList<EntitySegment>
}