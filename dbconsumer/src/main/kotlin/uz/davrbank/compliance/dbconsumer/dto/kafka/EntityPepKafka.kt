package uz.davrbank.compliance.dbconsumer.dto.kafka

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

data class EntityPepsKafka(
    @JsonProperty("EntityPEP")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityPep: List<EntityPepKafka>?,
)

data class EntityPepKafka(
    @JsonProperty("AdministrativeUnitLevel0")
    val administrativeUnitLevel0: String?,
    @JsonProperty("EffectiveDate")
    val effectiveDate: EffectiveDateKafka?,
    @JsonProperty("EntityPEPGUID")
    val entityPepguid: String?,
    @JsonProperty("EntityPEPSubcategories")
    val entityPepsubcategories: EntityPepSubcategoriesKafka?,
    @JsonProperty("ExpirationDate")
    val expirationDate: ExpirationDateKafka?,
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

data class EntityPepSubcategoriesKafka(
    @JsonProperty("Subcategory")
    val subcategory: PepSubcategoryKafka?,
)

data class PepSubcategoryKafka(
    @JsonProperty("EntityPEPSubCategoryGUID")
    val entityPepsubCategoryGuid: String?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String?,
    @JsonProperty("SubCategoryDesc")
    val subCategoryDesc: String?,
    @JsonProperty("SubCategoryLabel")
    val subCategoryLabel: String?,
)
