package uz.davrbank.compliance.dbconsumer.dto.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

data class EntityAdverseMediasKafka(
    @JsonProperty("EntityAdverseMedia")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityAdverseMedia: List<EntityAdverseMediaKafka>,
)

data class EntityAdverseMediaKafka @JsonCreator constructor(
    @JsonProperty("EntityAdverseMediaGUID")
    val entityAdverseMediaGuid: String?,
    @JsonProperty("AdverseMediaDesc")
    val adverseMediaDesc: String? = null,
    @JsonProperty("EntityAdverseMediaSubcategories")
    val entityAdverseMediaSubcategories: EntityAdverseMediaSubcategoriesKafka? = null,
    @JsonProperty("LastUpdated")
    val lastUpdated: String? = null,
) {
    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(value: String): EntityAdverseMediaKafka {
            return EntityAdverseMediaKafka(entityAdverseMediaGuid = value)
        }
    }
}

data class EntityAdverseMediaSubcategoriesKafka(
    @JsonProperty("Subcategory")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val subcategory: List<AdverseMediaSubcategoryKafka>,
)

data class AdverseMediaSubcategoryKafka(
    @JsonProperty("EntityAdverseMediaSubCategoryGUID")
    val entityAdverseMediaSubCategoryGuid: String?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String?,
    @JsonProperty("SubCategoryLabel")
    val subCategoryLabel: String?,
)