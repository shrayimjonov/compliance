package uz.davrbank.compliance.dbconsumer.dto.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

data class EntityEnforcementsKafka(
    @JsonProperty("EntityEnforcement")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityEnforcement: List<EntityEnforcementKafka>,
)

data class EntityEnforcementKafka @JsonCreator constructor(
    @JsonProperty("EntityEnforcementGUID")
    val entityEnforcementGUID: String?,              // nullable
    @JsonProperty("EnforcementDesc")
    val enforcementDesc: String = "",
    @JsonProperty("SourceName")
    val sourceName: String = "",
    @JsonProperty("SourceNameAbbrev")
    val sourceNameAbbrev: String = "",
    @JsonProperty("AdministrativeUnitName")
    val administrativeUnitName: String = "",
    @JsonProperty("ISOStandard")
    val isoStandard: String = "",
    @JsonProperty("LastUpdated")
    val lastUpdated: String = "",
    @JsonProperty("EntityEnforcementSubcategories")
    val entityEnforcementSubcategories: EntityEnforcementSubcategoriesKafka = EntityEnforcementSubcategoriesKafka(emptyList())
) {
    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(value: String): EntityEnforcementKafka {
            return EntityEnforcementKafka(entityEnforcementGUID = value)
        }
    }
}

data class EntityEnforcementSubcategoriesKafka(
    @JsonProperty("Subcategory")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
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