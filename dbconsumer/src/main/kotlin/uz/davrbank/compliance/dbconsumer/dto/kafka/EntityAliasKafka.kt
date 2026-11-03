package uz.davrbank.compliance.dbconsumer.dto.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

data class EntityAliasesKafka(
    @JsonProperty("EntityAlias")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityAlias: List<EntityAliasKafka> = emptyList(),
)

data class EntityAliasKafka @JsonCreator constructor(
    @JsonProperty("EntityAliasGUID")
    val entityAliasGuid: String?,

    @JsonProperty("AliasTypeDesc") val aliasTypeDesc: String? = null,
    @JsonProperty("EnglishDescription") val englishDescription: String? = null,
    @JsonProperty("FirstName") val firstName: String? = null,
    @JsonProperty("LastName") val lastName: String? = null,
    @JsonProperty("LastUpdated") val lastUpdated: String? = null,
    @JsonProperty("MiddleName") val middleName: String? = null,
    @JsonProperty("Name") val name: String? = null,
    @JsonProperty("Prefix") val prefix: String? = null,
    @JsonProperty("Suffix") val suffix: String? = null,
) {
    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(value: String): EntityAliasKafka {
            return EntityAliasKafka(entityAliasGuid = value)
        }
    }
}