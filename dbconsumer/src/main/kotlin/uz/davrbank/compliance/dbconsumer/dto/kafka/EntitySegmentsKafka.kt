package uz.davrbank.compliance.dbconsumer.dto.kafka

import com.fasterxml.jackson.annotation.JsonProperty

data class EntitySegmentsKafka(
    @JsonProperty("EntityAdverseMedias")
    var entityAdverseMedias: EntityAdverseMediasKafka?,
    @JsonProperty("EntityPEPs")
    var entityPeps: EntityPepsKafka?,
    @JsonProperty("EntityEnforcements")
    var entityEnforcements: EntityEnforcementsKafka?,
    @JsonProperty("EntitySanctions")
    var entitySanctions: EntitySanctionsKafka?,
    @JsonProperty("EntitySOEs")
    var entitySOEs: EntitySOEsKafka?,
    @JsonProperty("AdditionalSegments")
    var additionalSegments: AdditionalSegmentsKafka?
)
