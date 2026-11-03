package uz.davrbank.compliance.dbconsumer.dto.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

data class EntityRelationshipsKafka(
    @JsonProperty("EntityRelationship")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityRelationship: List<EntityRelationshipKafka>,
)

data class EntityRelationshipKafka @JsonCreator constructor(
    @JsonProperty("EntityRelationshipGUID")
    val entityRelationshipGuid: String?,
    @JsonProperty("GroupDesc")
    val groupDesc: String? = null,
    @JsonProperty("LastUpdated")
    val lastUpdated: String? = null,
    @JsonProperty("RelatedEntityGUID")
    val relatedEntityGuid: String? = null,
    @JsonProperty("RelationshipDesc")
    val relationshipDesc: String? = null,
) {
    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(value: String): EntityRelationshipKafka {
            return EntityRelationshipKafka(entityRelationshipGuid = value)
        }
    }
}