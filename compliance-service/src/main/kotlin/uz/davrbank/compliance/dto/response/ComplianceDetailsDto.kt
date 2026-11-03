package uz.davrbank.compliance.dto.response


import uz.davrbank.compliance.domain.*
import uz.davrbank.compliance.enums.*
import java.time.LocalDate
import java.util.*

data class ComplianceDetailsDto(
    val compliance: ComplianceDetailDto,
    val entityDobs: List<EntityDobDetailDto>,
    val entityAddresses: List<EntityAddressDetailDto>,
    val entityAliases: List<EntityAliasDetailDto>,
    val entityIdentifications: List<EntityIdentificationDetailDto>,
    val entityCountryAssociations: List<EntityCountryAssociationDetailDto>,
    val entityPositions: List<EntityPositionDetailDto>,
    val entityRelationships: List<EntityRelationshipDetailDto>,
    val entityRemarks: List<EntityRemarkDetailDto>,
    val entitySourceItems: List<EntitySourceItemDetailDto>,
    val entitySegment: EntitySegmentDetailDto?,
    val entityPeps: List<EntityPepDetailDto>,
    val entityPepSubcategories: List<EntityPepSubcategoryDetailDto>,
    val entityAdverseMedia: List<EntityAdverseMediaDetailDto>,
    val entityAdverseMediaSubcategories: List<EntityAdverseMediaSubcategoryDetailDto>,
    val entityEnforcements: List<EntityEnforcementDetailDto>,
    val entityEnforcementSubcategories: List<EntityEnforcementSubcategoryDetailDto>,
    val entitySanctions: List<EntitySanctionDetailDto>,
    val entitySoes: List<EntitySoeDetailDto>,
    val entitySoeSubcategories: List<EntitySoeSubcategoryDetailDto>,
    val entitySoeDomains: List<EntitySoeDomainDetailDto>,
    val additionalSegments: List<AdditionalSegmentDetailDto>,
    val records: List<RecordDetailDto>,
    val fields: List<FieldDetailDto>,
)

data class ComplianceDetailDto(
    val id: String,
    val type: Type,
    val gender: String,
    val name: String,
    val firstName: String?,
    val middleName: String?,
    val lastName: String,
    val fullName: String,
    val prefix: String?,
    val suffix: String?,
    val title: String?,
    val isDeceased: Boolean?,
    val deceasedDate: LocalDate?,
    val entityId: Long?,
    val lookupId: String?,
    val lastUpdated: Date,
    val complianceType: ComplianceType,
    val dataType: DataType,
    val tin: String?,
    val pinfl: String?,
    val createdDate: Date,
    val entityType: EntityType = EntityType.COMPLIANCE,
)

data class ComplianceReferenceDto(
    val complianceId: String,
    val name: String?,
)

data class EntityDobDetailDto(
    val id: String,
    val birthDate: LocalDate?,
    val lastUpdated: Date,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.ENTITY_DOB,
)

data class EntityAddressDetailDto(
    val id: String,
    val addressTypeDesc: String?,
    val city: String?,
    val stateProvinceRegion: String?,
    val country: String?,
    val isoStandard: String?,
    val lastUpdated: Date?,
    val postalCode: String?,
    val address1: String?,
    val address2: String?,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.ENTITY_ADDRESS,
)

data class EntityAliasDetailDto(
    val id: String,
    val aliasTypeDesc: String?,
    val englishDescription: String?,
    val firstName: String?,
    val lastName: String?,
    val lastUpdated: Date?,
    val middleName: String?,
    val name: String?,
    val prefix: String?,
    val suffix: String?,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.ENTITY_ALIAS,
)

data class EntityIdentificationDetailDto(
    val id: String,
    val administrativeUnitName: String?,
    val expiration: LocalDate?,
    val isoStandard: String?,
    val identificationIssuer: String?,
    val identificationNumber: String?,
    val identificationTypeDesc: String?,
    val issueDate: LocalDate?,
    val lastUpdated: Date?,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.ENTITY_IDENTIFICATION,
)

data class EntityCountryAssociationDetailDto(
    val id: String,
    val administrativeUnitName: String?,
    val associationTypeDesc: String?,
    val isostandard: String?,
    val lastUpdated: Date?,
    val ownershipPercentageCalc: String?,
    val complianceType: ComplianceType,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.ENTITY_COUNTRY_ASSOCIATION,
)

data class EntityPositionDetailDto(
    val id: String,
    val position: String?,
    val lastUpdated: Date?,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.ENTITY_POSITION,
)

data class EntityRelationshipDetailDto(
    val id: String,
    val relatedEntityGUID: String?,
    val groupDesc: String?,
    val relationshipDesc: String?,
    val lastUpdated: Date?,
    val ownershipPercentage: String?,
    val complianceType: ComplianceType,
    val compliance: ComplianceReferenceDto,
    val relatedEntity: ComplianceReferenceDto? = null,
    val relatedEntityName: String? = null,
    val relatedEntityFullName: String? = null,
    val entityType: EntityType = EntityType.ENTITY_RELATIONSHIP,
)

data class EntityRemarkDetailDto(
    val id: String,
    val remark: String?,
    val lastUpdated: Date?,
    val compliance: ComplianceReferenceDto,
    val remarkTranslations: RemarkTranslationsDto,
    val entityType: EntityType = EntityType.ENTITY_REMARK,
)

data class RemarkTranslationsDto(
    val originalText: String?,
    val translations: Map<Language, String>,
)

data class EntitySourceItemDetailDto(
    val id: String,
    val sourceURI: String?,
    val lastUpdated: Date?,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.ENTITY_SOURCE_ITEM,
)

data class EntitySegmentDetailDto(
    val id: Long?,
    val entityPep: Boolean,
    val entityAdverseMedia: Boolean,
    val entityEnforcement: Boolean,
    val entitySanction: Boolean,
    val entitySOE: Boolean,
    val additionalSegment: Boolean,
    val createdDate: Date?,
    val modifiedDate: Date?,
    val createdBy: String?,
    val modifiedBy: String?,
    val deleted: Boolean,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.ENTITY_SEGMENT,
)

data class EntityPepDetailDto(
    val id: String,
    val isPrimaryPEP: Long?,
    val isActivePEP: String?,
    val isInCountryPEPOnly: String?,
    val pepAdminLevelDesc: String?,
    val isoAdministrativeUnitLevel0: String?,
    val administrativeUnitLevel0: String?,
    val governingInstitution: String?,
    val governingRole: String?,
    val lastUpdated: Date?,
    val effectiveDate: LocalDate?,
    val expirationDate: LocalDate?,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.ENTITY_PEP,
)

data class EntityPepSubcategoryDetailDto(
    val id: String,
    val pepSubcategoryDesc: String?,
    val subCategoryLabel: String?,
    val lastUpdated: Date?,
    val entityPepId: String,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.ENTITY_PEP_SUBCATEGORY,
)

data class EntityAdverseMediaDetailDto(
    val id: String,
    val adverseMediaDesc: String?,
    val lastUpdated: Date?,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.ENTITY_ADVERSE_MEDIA,
)

data class EntityAdverseMediaSubcategoryDetailDto(
    val id: String,
    val subCategoryLabel: String?,
    val lastUpdated: Date?,
    val entityAdverseMediaId: String,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.ENTITY_ADVERSE_MEDIA_SUBCATEGORY,
)

data class EntityEnforcementDetailDto(
    val id: String,
    val enforcementDesc: String?,
    val sourceName: String?,
    val sourceNameAbbrev: String?,
    val administrativeUnitName: String?,
    val isoStandard: String?,
    val lastUpdated: Date?,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.ENTITY_ENFORCEMENT,
)

data class EntityEnforcementSubcategoryDetailDto(
    val id: String,
    val subCategoryLabel: String,
    val lastUpdated: Date,
    val entityEnforcementId: String,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.ENTITY_ENFORCEMENT_SUBCATEGORY,
)

data class EntitySanctionDetailDto(
    val id: String,
    val subCategoryLabel: String?,
    val sourceName: String?,
    val sourceNameAbbrev: String?,
    val administrativeUnitName: String?,
    val isoStandard: String?,
    val lastUpdated: Date,
    val complianceType: ComplianceType,
    val consolidatedSanction: ConsolidatedSanctionDetailDto,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.ENTITY_SANCTION,
)

data class ConsolidatedSanctionDetailDto(
    val consolidatedSanctionGUID: String?,
    val entityGUID: String?,
    val lastUpdated: Date?,
)

data class EntitySoeDetailDto(
    val id: String,
    val isActive: Boolean,
    val lastUpdated: Date,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.ENTITY_SOE,
)

data class EntitySoeSubcategoryDetailDto(
    val id: String,
    val label: String?,
    val lastUpdated: Date?,
    val entitySOEId: String,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.ENTITY_SOE_SUBCATEGORY,
)

data class EntitySoeDomainDetailDto(
    val id: String,
    val soeDomainDesc: String,
    val lastUpdated: Date,
    val entitySOEId: String,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.ENTITY_SOE_DOMAIN,
)

data class AdditionalSegmentDetailDto(
    val id: Long?,
    val type: String?,
    val createdDate: Date?,
    val modifiedDate: Date?,
    val createdBy: String?,
    val modifiedBy: String?,
    val deleted: Boolean,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.ADDITIONAL_SEGMENT,
)

data class RecordDetailDto(
    val id: String,
    val additionalSegmentId: Long?,
    val lastUpdated: Date?,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.RECORD,
)

data class FieldDetailDto(
    val id: Long?,
    val recordId: String,
    val derivedName: String?,
    val derivedValue: String?,
    val createdDate: Date?,
    val modifiedDate: Date?,
    val createdBy: String?,
    val modifiedBy: String?,
    val deleted: Boolean,
    val compliance: ComplianceReferenceDto,
    val entityType: EntityType = EntityType.FIELD,
)

internal fun Compliance.toDetailDto() = ComplianceDetailDto(
    id = id,
    type = type,
    gender = gender,
    name = name,
    firstName = firstName,
    middleName = middleName,
    lastName = lastName,
    fullName = fullName,
    prefix = prefix,
    suffix = suffix,
    title = title,
    isDeceased = isDeceased,
    deceasedDate = deceasedDate,
    entityId = entityId,
    lookupId = lookupId,
    lastUpdated = lastUpdated,
    complianceType = complianceType,
    dataType = dataType,
    tin = tin,
    pinfl = pinfl,
    createdDate = createdDate,
)

internal fun Compliance.toReferenceDto() = ComplianceReferenceDto(
    complianceId = id,
    name = name,
)

internal fun EntityDOB.toDetailDto() = EntityDobDetailDto(
    id = id,
    birthDate = birthDate,
    lastUpdated = lastUpdated,
    compliance = compliance.toReferenceDto(),
)

internal fun EntityAddress.toDetailDto() = EntityAddressDetailDto(
    id = id,
    addressTypeDesc = addressTypeDesc,
    city = city,
    stateProvinceRegion = stateProvinceRegion,
    country = country,
    isoStandard = isoStandard,
    lastUpdated = lastUpdated,
    postalCode = postalCode,
    address1 = address1,
    address2 = address2,
    compliance = compliance.toReferenceDto(),
)

internal fun EntityAlias.toDetailDto() = EntityAliasDetailDto(
    id = id,
    aliasTypeDesc = aliasTypeDesc,
    englishDescription = englishDescription,
    firstName = firstName,
    lastName = lastName,
    lastUpdated = lastUpdated,
    middleName = middleName,
    name = name,
    prefix = prefix,
    suffix = suffix,
    compliance = compliance.toReferenceDto(),
)

internal fun EntityIdentification.toDetailDto() = EntityIdentificationDetailDto(
    id = id,
    administrativeUnitName = administrativeUnitName,
    expiration = expiration,
    isoStandard = isoStandard,
    identificationIssuer = identificationIssuer,
    identificationNumber = identificationNumber,
    identificationTypeDesc = identificationTypeDesc,
    issueDate = issueDate,
    lastUpdated = lastUpdated,
    compliance = compliance.toReferenceDto(),
)

internal fun EntityCountryAssociation.toDetailDto() = EntityCountryAssociationDetailDto(
    id = id,
    administrativeUnitName = administrativeUnitName,
    associationTypeDesc = associationTypeDesc,
    isostandard = isostandard,
    lastUpdated = lastUpdated,
    ownershipPercentageCalc = ownershipPercentageCalc,
    complianceType = complianceType,
    compliance = compliance.toReferenceDto(),
)

internal fun EntityPosition.toDetailDto() = EntityPositionDetailDto(
    id = id,
    position = position,
    lastUpdated = lastUpdated,
    compliance = compliance.toReferenceDto(),
)

internal fun EntityRelationship.toDetailDto(relatedEntity: Compliance? = null) = EntityRelationshipDetailDto(
    id = id,
    relatedEntityGUID = relatedEntityGUID,
    groupDesc = groupDesc,
    relationshipDesc = relationshipDesc,
    lastUpdated = lastUpdated,
    ownershipPercentage = ownershipPercentage,
    complianceType = complianceType,
    compliance = compliance.toReferenceDto(),
    relatedEntity = relatedEntity?.toReferenceDto(),
    relatedEntityName = relatedEntity?.name,
    relatedEntityFullName = relatedEntity?.fullName,
)

internal fun EntityRemark.toDetailDto(entityTranslate: EntityTranslate? = null) = EntityRemarkDetailDto(
    id = id,
    remark = remark,
    lastUpdated = lastUpdated,
    compliance = compliance.toReferenceDto(),
    remarkTranslations = entityTranslate.toRemarkTranslationsDto(remark),
)

private fun EntityTranslate?.toRemarkTranslationsDto(fallbackOriginalText: String?) = RemarkTranslationsDto(
    originalText = this?.originalText ?: fallbackOriginalText,
    translations = this?.translations ?: emptyMap(),
)

internal fun EntitySourceItem.toDetailDto() = EntitySourceItemDetailDto(
    id = id,
    sourceURI = sourceURI,
    lastUpdated = lastUpdated,
    compliance = compliance.toReferenceDto(),
)

internal fun EntitySegment.toDetailDto() = EntitySegmentDetailDto(
    id = id,
    entityPep = entityPep,
    entityAdverseMedia = entityAdverseMedia,
    entityEnforcement = entityEnforcement,
    entitySanction = entitySanction,
    entitySOE = entitySOE,
    additionalSegment = additionalSegment,
    createdDate = createdDate,
    modifiedDate = modifiedDate,
    createdBy = createdBy,
    modifiedBy = modifiedBy,
    deleted = deleted,
    compliance = compliance.toReferenceDto(),
)

internal fun EntityPEP.toDetailDto() = EntityPepDetailDto(
    id = id,
    isPrimaryPEP = isPrimaryPEP,
    isActivePEP = isActivePEP,
    isInCountryPEPOnly = isInCountryPEPOnly,
    pepAdminLevelDesc = pepAdminLevelDesc,
    isoAdministrativeUnitLevel0 = isoAdministrativeUnitLevel0,
    administrativeUnitLevel0 = administrativeUnitLevel0,
    governingInstitution = governingInstitution,
    governingRole = governingRole,
    lastUpdated = lastUpdated,
    effectiveDate = effectiveDate,
    expirationDate = expirationDate,
    compliance = compliance.toReferenceDto(),
)

internal fun EntityPEPSubcategory.toDetailDto() = EntityPepSubcategoryDetailDto(
    id = id,
    pepSubcategoryDesc = pepSubcategoryDesc,
    subCategoryLabel = subCategoryLabel,
    lastUpdated = lastUpdated,
    entityPepId = entityPep.id,
    compliance = compliance.toReferenceDto(),
)

internal fun EntityAdverseMedia.toDetailDto() = EntityAdverseMediaDetailDto(
    id = id,
    adverseMediaDesc = adverseMediaDesc,
    lastUpdated = lastUpdated,
    compliance = compliance.toReferenceDto(),
)

internal fun EntityAdverseMediaSubcategory.toDetailDto() = EntityAdverseMediaSubcategoryDetailDto(
    id = id,
    subCategoryLabel = subCategoryLabel,
    lastUpdated = lastUpdated,
    entityAdverseMediaId = entityAdverseMedia.id,
    compliance = compliance.toReferenceDto(),
)

internal fun EntityEnforcement.toDetailDto() = EntityEnforcementDetailDto(
    id = id,
    enforcementDesc = enforcementDesc,
    sourceName = sourceName,
    sourceNameAbbrev = sourceNameAbbrev,
    administrativeUnitName = administrativeUnitName,
    isoStandard = isoStandard,
    lastUpdated = lastUpdated,
    compliance = compliance.toReferenceDto(),
)

internal fun EntityEnforcementSubcategory.toDetailDto() = EntityEnforcementSubcategoryDetailDto(
    id = id,
    subCategoryLabel = subCategoryLabel,
    lastUpdated = lastUpdated,
    entityEnforcementId = entityEnforcement.id,
    compliance = compliance.toReferenceDto(),
)

internal fun EntitySanction.toDetailDto() = EntitySanctionDetailDto(
    id = id,
    subCategoryLabel = subCategoryLabel,
    sourceName = sourceName,
    sourceNameAbbrev = sourceNameAbbrev,
    administrativeUnitName = administrativeUnitName,
    isoStandard = isoStandard,
    lastUpdated = lastUpdated,
    complianceType = complianceType,
    consolidatedSanction = consolidatedSanction.toDetailDto(),
    compliance = compliance.toReferenceDto(),
)

internal fun ConsolidatedSanction.toDetailDto() = ConsolidatedSanctionDetailDto(
    consolidatedSanctionGUID = consolidatedSanctionGUID,
    entityGUID = entityGUID,
    lastUpdated = lastUpdated,
)

internal fun EntitySOE.toDetailDto() = EntitySoeDetailDto(
    id = id,
    isActive = isActive,
    lastUpdated = lastUpdated,
    compliance = compliance.toReferenceDto(),
)

internal fun EntitySOESubcategory.toDetailDto() = EntitySoeSubcategoryDetailDto(
    id = id,
    label = label,
    lastUpdated = lastUpdated,
    entitySOEId = entitySOE.id,
    compliance = compliance.toReferenceDto(),
)

internal fun EntitySOEDomain.toDetailDto() = EntitySoeDomainDetailDto(
    id = id,
    soeDomainDesc = soeDomainDesc,
    lastUpdated = lastUpdated,
    entitySOEId = entitySOE.id,
    compliance = compliance.toReferenceDto(),
)

internal fun AdditionalSegment.toDetailDto() = AdditionalSegmentDetailDto(
    id = id,
    type = type,
    createdDate = createdDate,
    modifiedDate = modifiedDate,
    createdBy = createdBy,
    modifiedBy = modifiedBy,
    deleted = deleted,
    compliance = compliance.toReferenceDto(),
)

internal fun Record.toDetailDto() = RecordDetailDto(
    id = id,
    additionalSegmentId = additionalSegment.id,
    lastUpdated = lastUpdated,
    compliance = compliance.toReferenceDto(),
)

internal fun Field.toDetailDto() = FieldDetailDto(
    id = id,
    recordId = record.id,
    derivedName = derivedName,
    derivedValue = derivedValue,
    createdDate = createdDate,
    modifiedDate = modifiedDate,
    createdBy = createdBy,
    modifiedBy = modifiedBy,
    deleted = deleted,
    compliance = compliance.toReferenceDto(),
)
