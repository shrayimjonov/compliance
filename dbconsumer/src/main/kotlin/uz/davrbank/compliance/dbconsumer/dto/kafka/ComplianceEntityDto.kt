package uz.davrbank.compliance.dbconsumer.dto.kafka

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import uz.davrbank.compliance.dbconsumer.enums.Type
import uz.davrbank.compliance.dbconsumer.util.IntToBooleanDeserializer
import uz.davrbank.compliance.dbconsumer.util.TypeDeserializer

data class ComplianceEntityDto(
    @JsonProperty("DeceasedDate")
    val deceasedDate: DeceasedDateKafka?,
    @JsonProperty("EntityAddresses")
    val entityAddresses: EntityAddressesKafka?,
    @JsonProperty("EntityAliases")
    val entityAliases: EntityAliasesKafka?,
    @JsonProperty("EntityCountryAssociations")
    val entityCountryAssociations: EntityCountryAssociationsKafka?,
    @JsonProperty("EntityDOBs")
    val entityDobs: EntityDobsKafka?,
    @JsonProperty("EntityGUID")
    val entityGuid: String,
    @JsonProperty("EntityID")
    val entityId: Long,
    @JsonProperty("EntityIdentifications")
    val entityIdentifications: EntityIdentificationsKafka?,
    @JsonProperty("EntityPositions")
    val entityPositions: EntityPositionsKafka?,
    @JsonProperty("EntityRelationships")
    val entityRelationships: EntityRelationshipsKafka?,
    @JsonProperty("EntityRemarks")
    val entityRemarks: EntityRemarksKafka?,
    @JsonProperty("EntitySegments")
    val entitySegments: EntitySegmentsKafka?,
    @JsonProperty("EntitySourceItems")
    val entitySourceItems: EntitySourceItemsKafka?,
    @JsonProperty("EntityTypeDesc")
    @JsonDeserialize(using = TypeDeserializer::class)
    val entityTypeDesc: Type,
    @JsonProperty("FirstName")
    val firstName: String?,
    @JsonProperty("Gender")
    val gender: String,
    @JsonDeserialize(using = IntToBooleanDeserializer::class)
    @JsonProperty("IsDeceased")
    val isDeceased: Boolean?,
    @JsonProperty("LastName")
    val lastName: String?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String,
    @JsonProperty("LookupID")
    val lookupId: String?,
    @JsonProperty("MiddleName")
    val middleName: String?,
    @JsonProperty("Name")
    val name: String,
    @JsonProperty("Prefix")
    val prefix: String?,
    @JsonProperty("Suffix")
    val suffix: String?,
    @JsonProperty("Title")
    val title: String?
)
