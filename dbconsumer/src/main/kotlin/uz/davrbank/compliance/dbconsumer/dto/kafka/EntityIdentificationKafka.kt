package uz.davrbank.compliance.dbconsumer.dto.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

data class EntityIdentificationsKafka(
    @JsonProperty("EntityIdentification")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityIdentification: List<EntityIdentificationKafka>,
)

data class EntityIdentificationKafka @JsonCreator constructor(
    @JsonProperty("EntityIdentificationGUID")
    val entityIdentificationGuid: String?,
    @JsonProperty("AdministrativeUnitName")
    val administrativeUnitName: String? = null,
    @JsonProperty("ExpirationDate")
    val expirationDate: ExpirationDateKafka = ExpirationDateKafka(),
    @JsonProperty("ISOStandard")
    val isostandard: String? = null,
    @JsonProperty("IdentificationIssuer")
    val identificationIssuer: String? = null,
    @JsonProperty("IdentificationNumber")
    val identificationNumber: String? = null,
    @JsonProperty("IdentificationTypeDesc")
    val identificationTypeDesc: String? = null,
    @JsonProperty("IssueDate")
    val issueDate: IssueDateKafka = IssueDateKafka(),
    @JsonProperty("LastUpdated")
    val lastUpdated: String? = null,
) {
    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(value: String): EntityIdentificationKafka {
            return EntityIdentificationKafka(entityIdentificationGuid = value)
        }
    }
}

data class ExpirationDateKafka(
    @JsonProperty("ExpirationDay")
    val expirationDay: Int? = null,
    @JsonProperty("ExpirationMonth")
    val expirationMonth: Int? = null,
    @JsonProperty("ExpirationYear")
    val expirationYear: Int? = null,
)

data class IssueDateKafka(
    @JsonProperty("IssueYear")
    val issueYear: Int? = null,
    @JsonProperty("IssueMonth")
    val issueMonth: Int? = null,
    @JsonProperty("IssueDay")
    val issueDay: Int? = null,
)