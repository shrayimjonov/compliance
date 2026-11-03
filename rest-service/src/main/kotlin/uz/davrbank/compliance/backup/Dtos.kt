package uz.davrbank.compliance.backup




import java.util.*


data class AdditionalDataToEntityDto(
    val entityDOBs: MutableList<EntityDOB>,
    val entityAddresses: MutableList<EntityAddress>,
    val entityCountryAssociations: MutableList<EntityCountryAssociation>,
    val entityPositions: MutableList<EntityPosition>,
    val entityRelationships: MutableList<EntityRelationship>,
    val entityRemarks: MutableList<EntityRemark>,
    val entityAlias: MutableList<EntityAlias>,
    val entityIdentification: MutableList<EntityIdentification>,
    val entitySourceItem: MutableList<EntitySourceItem>,
    val entitySegment: EntitySegmentDto,
    val entityComplianceEntity: Compliance
)

data class EntitySegmentDto(
    var entityPepInternal: MutableList<EntityPepInternalDto>? = null,
    var entityAdverseMediaInternalDto: MutableList<EntityAdverseMediaInternalDto>? = null,
    var entityEnforcementInternalDto: MutableList<EntityEnforcementInternalDto>? = null,
    var entitySanctionInternalDto: MutableList<EntitySanctionInternalDto>? = null,
    var entitySOEsInternalDto: MutableList<EntitySOEsInternalDto>? = null,
    var additionalSegmentInternalDto: MutableList<AdditionalSegmentInternalDto>? = null
)

data class AdditionalSegmentInternalDto(
    var additionalSegment: AdditionalSegment,
    var recordInternal: List<RecordInternalDto>
)

data class RecordInternalDto(
    val record: Record,
    val field: List<Field>,
)

data class EntitySOEsInternalDto(
    var entitySOE: EntitySOE,
    var entitySOESubcategory: List<EntitySOESubcategory>,
    var entitySOEDomain: List<EntitySOEDomain>
)

data class EntitySanctionInternalDto(
    var entitySanction: EntitySanction
)


data class EntityEnforcementInternalDto(
    var entityEnforcement: EntityEnforcement, var entityEnforcementSubcategory: List<EntityEnforcementSubcategory>
)

data class EntityPepInternalDto(
    val entityPep: EntityPEP,
    val entityPEPSubcategory: EntityPEPSubcategory
)

data class EntityAdverseMediaInternalDto(
    val entityAdverseMedia: EntityAdverseMedia,
    val entityAdverseMediaSubcategory: List<EntityAdverseMediaSubcategory>
)

data class ComplianceRelatedEntityDataRiskResponse(
    var groupDesc: String?,
    var relationshipDesc: String?,
    var lastUpdated: Date?,
    var entityRelationshipGUID: String?,
    var id: String?,
    val entityTypeDesc: String
) {
    companion object {
        fun toDto(entityRelationship: EntityRelationship): ComplianceRelatedEntityDataRiskResponse {
            return ComplianceRelatedEntityDataRiskResponse(
                entityRelationship.groupDesc,
                entityRelationship.relationshipDesc,
                entityRelationship.lastUpdated,
                entityRelationship.id,
                id = entityRelationship.relatedEntityGUID,
                entityRelationship.compliance.type.name
            )
        }
    }
}

