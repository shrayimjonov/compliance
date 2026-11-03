package uz.davrbank.compliance.dbconsumer.dto.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import uz.davrbank.compliance.dbconsumer.config.UnescapeHtmlDeserializer

data class EntitySourceItemsKafka(
    @JsonProperty("EntitySourceItem")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entitySourceItem: List<EntitySourceItemKafka>,
)

data class EntitySourceItemKafka @JsonCreator constructor(
    @JsonProperty("EntitySourceItemGUID")
    val entitySourceItemGuid: String?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String? = null,
    @JsonProperty("SourceURI")
    @JsonDeserialize(using = UnescapeHtmlDeserializer::class)
    val sourceUri: String? = null,
) {
    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(value: String): EntitySourceItemKafka {
            return EntitySourceItemKafka(entitySourceItemGuid = value)
        }
    }
}