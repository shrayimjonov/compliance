package uz.davrbank.compliance.kafka

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import uz.davrbank.compliance.IntToBooleanDeserializer
import uz.davrbank.compliance.TypeDeserializer
import uz.davrbank.compliance.backup.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ComplianceEntityDto(
    @JsonProperty("DeceasedDate")
    val deceasedDate: DeceasedDateKafka?,
    @JsonProperty("EntityAddresses")
    val entityAddresses: EntityAddressesKafka?,
    @JsonProperty("EntityAliases")
    val entityAliases: EntityAliasesKafka?,
    @JsonProperty("EntityCountryAssociations")
    val entityCountryAssociations: EntityCountryAssociationsKafka?,
    @JsonProperty("EntityDOBs")
    val entityDobs: EntityDobsKafka?,
    @JsonProperty("EntityGUID")
    val entityGuid: String,
    @JsonProperty("EntityID")
    val entityId: Long,
    @JsonProperty("EntityIdentifications")
    val entityIdentifications: EntityIdentificationsKafka?,
    @JsonProperty("EntityPositions")
    val entityPositions: EntityPositionsKafka?,
    @JsonProperty("EntityRelationships")
    val entityRelationships: EntityRelationshipsKafka?,
    @JsonProperty("EntityRemarks")
    val entityRemarks: EntityRemarksKafka?,
    @JsonProperty("EntitySegments")
    val entitySegments: EntitySegmentsKafka?,
    @JsonProperty("EntitySourceItems")
    val entitySourceItems: EntitySourceItemsKafka?,
    @JsonProperty("EntityTypeDesc")
    @JsonDeserialize(using = TypeDeserializer::class)
    val entityTypeDesc: Type,
    @JsonProperty("FirstName")
    val firstName: String?,
    @JsonProperty("Gender")
    val gender: String,
    @JsonDeserialize(using = IntToBooleanDeserializer::class)
    @JsonProperty("IsDeceased")
    val isDeceased: Boolean?,
    @JsonProperty("LastName")
    val lastName: String?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String,
    @JsonProperty("LookupID")
    val lookupId: String?,
    @JsonProperty("MiddleName")
    val middleName: String?,
    @JsonProperty("Name")
    val name: String,
    @JsonProperty("Prefix")
    val prefix: String?,
    @JsonProperty("Suffix")
    val suffix: String?,
    @JsonProperty("Title")
    val title: String?,
)

data class DeceasedDateKafka(
    @JsonProperty("DeceasedDay")
    val deceasedDay: Int?,
    @JsonProperty("DeceasedMonth")
    val deceasedMonth: Int?,
    @JsonProperty("DeceasedYear")
    val deceasedYear: Int?,
)

data class EntityAddressesKafka(
    @JsonProperty("EntityAddress") @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityAddress: List<EntityAddressKafka>,
)

data class EntityAddressKafka(
    @JsonProperty("Address1")
    val address1: String?,
    @JsonProperty("Address2")
    val address2: String?,
    @JsonProperty("AddressTypeDesc")
    val addressTypeDesc: String?,
    @JsonProperty("City")
    val city: String?,
    @JsonProperty("Country")
    val country: String?,
    @JsonProperty("EntityAddressGUID")
    val entityAddressGuid: String?,
    @JsonProperty("ISOStandard")
    val isostandard: String?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String?,
    @JsonProperty("PostalCode")
    val postalCode: String?,
    @JsonProperty("StateProvinceRegion")
    val stateProvinceRegion: String?,
)

data class EntityAliasesKafka(
    @JsonProperty("EntityAlias") @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityAlias: List<EntityAliasKafka>,
)

data class EntityAliasKafka(
    @JsonProperty("AliasTypeDesc")
    val aliasTypeDesc: String?,
    @JsonProperty("EnglishDescription")
    val englishDescription: String?,
    @JsonProperty("EntityAliasGUID")
    val entityAliasGuid: String?,
    @JsonProperty("FirstName")
    val firstName: String?,
    @JsonProperty("LastName")
    val lastName: String?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String?,
    @JsonProperty("MiddleName")
    val middleName: String?,
    @JsonProperty("Name")
    val name: String?,
    @JsonProperty("Prefix")
    val prefix: String?,
    @JsonProperty("Suffix")
    val suffix: String?,
)

data class EntityCountryAssociationsKafka(
    @JsonProperty("EntityCountryAssociation") @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityCountryAssociation: List<EntityCountryAssociationKafka>,
)

data class EntityCountryAssociationKafka(
    @JsonProperty("AdministrativeUnitName")
    val administrativeUnitName: String?,
    @JsonProperty("AssociationTypeDesc")
    val associationTypeDesc: String?,
    @JsonProperty("EntityCountryAssociationGUID")
    val entityCountryAssociationGuid: String?,
    @JsonProperty("ISOStandard")
    val isostandard: String?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String?,
)

data class EntityDobsKafka(
    @JsonProperty("EntityDOB") @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityDob: List<EntityDobKafka>,
)

data class EntityDobKafka(
    @JsonProperty("BirthDate")
    val birthDate: BirthDateKafka?,
    @JsonProperty("EntityDOBGUID")
    val entityDobguid: String,
    @JsonProperty("LastUpdated")
    val lastUpdated: String,
)

data class BirthDateKafka(
    @field:JsonProperty("BirthDay")
    val birthDay: Int?,
    @field:JsonProperty("BirthMonth")
    val birthMonth: Int?,
    @field:JsonProperty("BirthYear")
    val birthYear: Int?,
)

//data class ExpirationDate(
//    @JsonProperty("BirthDay")
//    val birthDay: Long?,
//    @JsonProperty("BirthMonth")
//    val birthMonth: Long?,
//    @JsonProperty("BirthYear")
//    val birthYear: Long?,
//)

data class EntityIdentificationsKafka(
    @JsonProperty("EntityIdentification") @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityIdentification: List<EntityIdentificationKafka>,
)

data class EntityIdentificationKafka(
    @JsonProperty("AdministrativeUnitName")
    val administrativeUnitName: String?,
    @JsonProperty("EntityIdentificationGUID")
    val entityIdentificationGuid: String?,
    @JsonProperty("ExpirationDate")
    val expirationDate: ExpirationDateKafka,
    @JsonProperty("ISOStandard")
    val isostandard: String?,
    @JsonProperty("IdentificationIssuer")
    val identificationIssuer: String?,
    @JsonProperty("IdentificationNumber")
    val identificationNumber: String?,
    @JsonProperty("IdentificationTypeDesc")
    val identificationTypeDesc: String?,
    @JsonProperty("IssueDate")
    val issueDate: IssueDate,
    @JsonProperty("LastUpdated")
    val lastUpdated: String?,
)

data class EntityPositionsKafka(
    @JsonProperty("EntityPosition") @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityPosition: EntityPositionKafka?,
)

data class EntityPositionKafka(
    @JsonProperty("EntityPositionGUID")
    val entityPositionGuid: String?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String?,
    @JsonProperty("Position")
    val position: String?,
)

data class EntityRelationshipsKafka(
    @JsonProperty("EntityRelationship") @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityRelationship: List<EntityRelationshipKafka>,
)

data class EntityRelationshipKafka(
    @JsonProperty("EntityRelationshipGUID")
    val entityRelationshipGuid: String?,
    @JsonProperty("GroupDesc")
    val groupDesc: String?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String?,
    @JsonProperty("RelatedEntityGUID")
    val relatedEntityGuid: String?,
    @JsonProperty("RelationshipDesc")
    val relationshipDesc: String?,
)

data class EntityRemarksKafka(
    @JsonProperty("EntityRemark")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityRemark: List<EntityRemarkKafka>,
)

data class EntityRemarkKafka(
    @JsonProperty("EntityRemarkGUID")
    val entityRemarkGuid: String?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String?,
    @JsonProperty("Remark")
    val remark: String?,
)

data class EntitySegmentsKafka(
    @JsonProperty("EntityAdverseMedias")
    var entityAdverseMedias: EntityAdverseMediasKafka?,
    @JsonProperty("EntityPEPs")
    var entityPeps: EntityPepsKafka?,
    @JsonProperty("EntityEnforcements")
    var entityEnforcements: EntityEnforcementsKafka?,
    @JsonProperty("EntitySanctions")
    var entitySanctions: EntitySanctionsKafka?,
    @JsonProperty("EntitySOEs")
    var entitySOEs: EntitySOEsKafka?,
    @JsonProperty("AdditionalSegments")
    var additionalSegments: AdditionalSegmentsKafka?
)

data class EntitySanctionsKafka(
    @JsonProperty("EntitySanction")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entitySanction: List<EntitySanctionKafka>,
)

data class EntitySanctionKafka(
    @JsonProperty("EntitySanctionGUID")
    val entitySanctionGUID: String,
    @JsonProperty("SubCategoryLabel")
    val subCategoryLabel: String,
    @JsonProperty("SourceName")
    val sourceName: String,
    @JsonProperty("SourceNameAbbrev")
    val sourceNameAbbrev: String,
    @JsonProperty("AdministrativeUnitName")
    val administrativeUnitName: String,
    @JsonProperty("ISOStandard")
    val isoStandard: String,
    @JsonProperty("LastUpdated")
    val lastUpdated: String,
    @JsonProperty("ConsolidatedSanction")
    val consolidatedSanction: ConsolidatedSanctionKafka?
)

data class ConsolidatedSanctionKafka(
    @JsonProperty("ConsolidatedSanctionGUID")
    val consolidatedSanctionGUID: String,
    @JsonProperty("EntityGUID")
    val entityGUID: String,
    @JsonProperty("LastUpdated")
    val lastUpdated: String
)


data class EntitySOEsKafka(
    @JsonProperty("EntitySOE")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entitySOE: List<EntitySOEKafka>?
)

data class EntitySOEKafka(
    @JsonProperty("EntitySOEGUID")
    val entitySOEGUID: String,
    @JsonProperty("IsActive")
    @JsonDeserialize(using = IntToBooleanDeserializer::class)
    val isActive: Boolean,
    @JsonProperty("LastUpdated")
    val lastUpdated: String,
    @JsonProperty("EntitySOEDomains")
    val entitySOEDomains: EntitySOEDomainsKafka?,
    @JsonProperty("EntitySOESubcategories")
    val entitySOESubcategories: EntitySOESubcategoriesDTO?
)

data class EntitySOEDomainsKafka(
    @JsonProperty("EntitySOEDomain") @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entitySOEDomain: List<EntitySOEDomainKafka>?
)

data class EntitySOEDomainKafka(
    @JsonProperty("EntitySOEDomainGUID")
    val entitySOEDomainGUID: String,
    @JsonProperty("SOEDomainDesc")
    val soeDomainDesc: String,
    @JsonProperty("LastUpdated")
    val lastUpdated: String
)

data class EntitySOESubcategoriesDTO(
    @JsonProperty("Subcategory")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val subcategory: List<EntitySOESubcategoryKafka>?
)

data class EntitySOESubcategoryKafka(
    @JsonProperty("EntitySOESubCategoryGUID")
    val entitySOESubcategoryGUID: String?,
    @JsonProperty("SubCategoryLabel")
    val subCategoryLabel: String?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String?,
)


data class AdditionalSegmentsKafka(
    @JsonProperty("Segment")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val segment: List<SegmentKafka>
)

data class SegmentKafka(
    @JsonProperty("Record")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val record: List<RecordKafka>?,
    @JsonProperty("_Type")
    val type: String?
)

data class RecordKafka(
    @JsonProperty("LastUpdated")
    val lastUpdated: String,
    @JsonProperty("Field")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val field: List<FieldKafka>?,
    @JsonProperty("_GUID")
    val guid: String?
)

data class FieldKafka(
    @JsonProperty("DerivedName")
    val derivedName: String,
    @JsonProperty("DerivedValue")
    val derivedValue: String
)


data class EntityAdverseMediasKafka(
    @JsonProperty("EntityAdverseMedia")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityAdverseMedia: List<EntityAdverseMediaKafka>,
)

data class EntityEnforcementsKafka(
    @JsonProperty("EntityEnforcement")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityEnforcement: List<EntityEnforcementKafka>,
)

data class EntityEnforcementKafka(
    @JsonProperty("EntityEnforcementGUID")
    val entityEnforcementGUID: String,
    @JsonProperty("EnforcementDesc")
    val enforcementDesc: String,
    @JsonProperty("SourceName")
    val sourceName: String,
    @JsonProperty("SourceNameAbbrev")
    val sourceNameAbbrev: String,
    @JsonProperty("AdministrativeUnitName")
    val administrativeUnitName: String,
    @JsonProperty("ISOStandard")
    val isoStandard: String,
    @JsonProperty("LastUpdated")
    val lastUpdated: String,
    @JsonProperty("EntityEnforcementSubcategories")
    val entityEnforcementSubcategories: EntityEnforcementSubcategoriesKafka
)

data class EntityEnforcementSubcategoriesKafka(
    @JsonProperty("Subcategory") @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val subcategory: List<EntityEnforcementSubcategoryKafka>
)

data class EntityEnforcementSubcategoryKafka(
    @JsonProperty("EntityEnforcementSubCategoryGUID")
    val entityEnforcementSubCategoryGUID: String,
    @JsonProperty("SubCategoryLabel")
    val subCategoryLabel: String,
    @JsonProperty("LastUpdated")
    val lastUpdated: String
)


data class EntityAdverseMediaKafka(
    @JsonProperty("AdverseMediaDesc")
    val adverseMediaDesc: String?,
    @JsonProperty("EntityAdverseMediaGUID")
    val entityAdverseMediaGuid: String?,
    @JsonProperty("EntityAdverseMediaSubcategories")
    val entityAdverseMediaSubcategories: EntityAdverseMediaSubcategories?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String?,
)

data class EntityAdverseMediaSubcategories(
    @JsonProperty("Subcategory") @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val subcategory: List<Subcategory>,
)

data class Subcategory(
    @JsonProperty("EntityAdverseMediaSubCategoryGUID")
    val entityAdverseMediaSubCategoryGuid: String?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String?,
    @JsonProperty("SubCategoryLabel")
    val subCategoryLabel: String?,
)

data class EntityPepsKafka(
    @JsonProperty("EntityPEP")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityPep: List<EntityPepKafka>?,
)

data class EntityPepKafka(
    @JsonProperty("AdministrativeUnitLevel0")
    val administrativeUnitLevel0: String?,
    @JsonProperty("AdministrativeUnitLevel1")
    val administrativeUnitLevel1: String?,
    @JsonProperty("AdministrativeUnitLevel2")
    val administrativeUnitLevel2: String?,
    @JsonProperty("AdministrativeUnitLevel3")
    val administrativeUnitLevel3: String?,
    @JsonProperty("AdministrativeUnitLevel4")
    val administrativeUnitLevel4: String?,
    @JsonProperty("EffectiveDate")
    val effectiveDate: EffectiveDateKafka?,
    @JsonProperty("EffectiveDateTypeDesc")
    val effectiveDateTypeDesc: String?,
    @JsonProperty("EntityPEPGUID")
    val entityPepguid: String?,
    @JsonProperty("EntityPEPSubcategories")
    val entityPepsubcategories: EntityPepsubcategories?,
    @JsonProperty("ExpirationDate")
    val expirationDate: ExpirationDateKafka?,
    @JsonProperty("ExpirationDateTypeDesc")
    val expirationDateTypeDesc: String?,
    @JsonProperty("GoverningInstitution")
    val governingInstitution: String?,
    @JsonProperty("GoverningRole")
    val governingRole: String?,
    @JsonProperty("ISOAdministrativeUnitLevel0")
    val isoadministrativeUnitLevel0: String?,
    @JsonProperty("IsActivePEP")
    val isActivePep: String?,
    @JsonProperty("IsInCountryPEPOnly")
    val isInCountryPeponly: String?,
    @JsonProperty("IsPrimaryPEP")
    val isPrimaryPep: Long?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String?,
    @JsonProperty("PEPAdminLevelDesc")
    val pepadminLevelDesc: String?,
)

data class EffectiveDateKafka(
    @JsonProperty("EffectiveDay")
    val effectiveDay: Int?,
    @JsonProperty("EffectiveMonth")
    val effectiveMonth: Int?,
    @JsonProperty("EffectiveYear")
    val effectiveYear: Int?,
)

data class EntityPepsubcategories(
    @JsonProperty("Subcategory")
    val subcategory: Subcategory2?,
)

data class Subcategory2(
    @JsonProperty("EntityPEPSubCategoryGUID")
    val entityPepsubCategoryGuid: String?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String?,
    @JsonProperty("SubCategoryDesc")
    val subCategoryDesc: String?,
    @JsonProperty("SubCategoryLabel")
    val subCategoryLabel: String?,
)

data class ExpirationDateKafka(
    @JsonProperty("ExpirationDay")
    val expirationDay: Int?,
    @JsonProperty("ExpirationMonth")
    val expirationMonth: Int?,
    @JsonProperty("ExpirationYear")
    val expirationYear: Int?,
)

data class IssueDate(
    @JsonProperty("IssueYear")
    val issueYear: Int?,
    @JsonProperty("IssueMonth")
    val issueMonth: Int?,
    @JsonProperty("IssueDay")
    val issueDay: Int?,
)

data class EntitySourceItemsKafka(
    @JsonProperty("EntitySourceItem") @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entitySourceItem: List<EntitySourceItemKafka>,
)

data class EntitySourceItemKafka(
    @JsonProperty("EntitySourceItemGUID")
    val entitySourceItemGuid: String?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String?,
    @JsonProperty("SourceURI")
    val sourceUri: String?,
)


data class DeleteEventKafka(
    val DateDeleted: String,
    val EntityGUID: String
) {
    fun getParsedDate(): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        return LocalDateTime.parse(DateDeleted, formatter)
    }
}