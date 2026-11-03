package uz.davrbank.compliance.dbconsumer.dto.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

data class EntityDobsKafka(
    @JsonProperty("EntityDOB")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityDob: List<EntityDobKafka>,
)

data class EntityDobKafka @JsonCreator constructor(
    @JsonProperty("EntityDOBGUID")
    val entityDobguid: String?,           // nullable here
    @JsonProperty("BirthDate")
    val birthDate: BirthDateKafka? = null,
    @JsonProperty("LastUpdated")
    val lastUpdated: String? = null,
) {
    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(value: String): EntityDobKafka {
            return EntityDobKafka(entityDobguid = value)
        }
    }
}

data class BirthDateKafka(
    @field:JsonProperty("BirthDay")
    val birthDay: Int? = null,
    @field:JsonProperty("BirthMonth")
    val birthMonth: Int? = null,
    @field:JsonProperty("BirthYear")
    val birthYear: Int? = null,
)