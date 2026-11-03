package uz.davrbank.compliance.dbconsumer.dto.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

data class EntityRemarksKafka(
    @JsonProperty("EntityRemark")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityRemark: List<EntityRemarkKafka>,
)

data class EntityRemarkKafka @JsonCreator constructor(
    @JsonProperty("EntityRemarkGUID")
    val entityRemarkGuid: String?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String? = null,
    @JsonProperty("Remark")
    val remark: String? = null,
) {
    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(value: String): EntityRemarkKafka {
            return EntityRemarkKafka(entityRemarkGuid = value)
        }
    }
}