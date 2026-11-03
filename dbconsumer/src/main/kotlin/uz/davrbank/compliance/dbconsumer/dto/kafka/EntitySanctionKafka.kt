package uz.davrbank.compliance.dbconsumer.dto.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

data class EntitySanctionsKafka(
    @JsonProperty("EntitySanction")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entitySanction: List<EntitySanctionKafka>,
)

data class EntitySanctionKafka @JsonCreator constructor(
    @JsonProperty("EntitySanctionGUID")
    val entitySanctionGUID: String?,              // nullable
    @JsonProperty("SubCategoryLabel")
    val subCategoryLabel: String = "",
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
    @JsonProperty("ConsolidatedSanction")
    val consolidatedSanction: ConsolidatedSanctionKafka? = null
) {
    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(value: String): EntitySanctionKafka {
            return EntitySanctionKafka(entitySanctionGUID = value)
        }
    }
}

data class ConsolidatedSanctionKafka(
    @JsonProperty("ConsolidatedSanctionGUID")
    val consolidatedSanctionGUID: String,
    @JsonProperty("EntityGUID")
    val entityGUID: String,
    @JsonProperty("LastUpdated")
    val lastUpdated: String
)