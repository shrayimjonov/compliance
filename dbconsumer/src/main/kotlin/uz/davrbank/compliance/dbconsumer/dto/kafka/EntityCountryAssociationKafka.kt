package uz.davrbank.compliance.dbconsumer.dto.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

data class EntityCountryAssociationsKafka(
    @JsonProperty("EntityCountryAssociation")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entityCountryAssociation: List<EntityCountryAssociationKafka>,
)

data class EntityCountryAssociationKafka @JsonCreator constructor(
    @JsonProperty("EntityCountryAssociationGUID")
    val entityCountryAssociationGuid: String?,
    @JsonProperty("AdministrativeUnitName")
    val administrativeUnitName: String? = null,
    @JsonProperty("AssociationTypeDesc")
    val associationTypeDesc: String? = null,
    @JsonProperty("ISOStandard")
    val isostandard: String? = null,
    @JsonProperty("LastUpdated")
    val lastUpdated: String? = null,
) {
    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(value: String): EntityCountryAssociationKafka {
            return EntityCountryAssociationKafka(entityCountryAssociationGuid = value)
        }
    }
}