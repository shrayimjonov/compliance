package uz.davrbank.compliance.dbconsumer.dto.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

data class EntityAddressesKafka(
    @JsonProperty("EntityAddress")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityAddress: List<EntityAddressKafka> = emptyList(),
)

data class EntityAddressKafka @JsonCreator constructor(
    @JsonProperty("EntityAddressGUID")
    val entityAddressGuid: String?,
    @JsonProperty("AddressTypeDesc") val addressTypeDesc: String? = null,
    @JsonProperty("City") val city: String? = null,
    @JsonProperty("StateProvinceRegion") val stateProvinceRegion: String? = null,
    @JsonProperty("Country") val country: String? = null,
    @JsonProperty("ISOStandard") val isostandard: String? = null,
    @JsonProperty("LastUpdated") val lastUpdated: String? = null,
    @JsonProperty("PostalCode") val postalCode: String? = null,
    @JsonProperty("Address1") val address1: String? = null,
    @JsonProperty("Address2") val address2: String? = null,
) {
    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(value: String): EntityAddressKafka {
            return EntityAddressKafka(entityAddressGuid = value)
        }
    }
}