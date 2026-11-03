package uz.davrbank.compliance.dbconsumer.dto.kafka

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

data class EntityPositionsKafka(
    @JsonProperty("EntityPosition")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
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
