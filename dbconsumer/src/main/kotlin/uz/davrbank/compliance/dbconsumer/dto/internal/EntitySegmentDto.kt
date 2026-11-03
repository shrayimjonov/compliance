package uz.davrbank.compliance.dbconsumer.dto.internal

data class EntitySegmentDto(
    var entityPepInternal: MutableList<EntityPepInternalDto>? = null,
    var entityAdverseMediaInternalDto: MutableList<EntityAdverseMediaInternalDto>? = null,
    var entityEnforcementInternalDto: MutableList<EntityEnforcementInternalDto>? = null,
    var entitySanctionInternalDto: MutableList<EntitySanctionInternalDto>? = null,
    var entitySOEsInternalDto: MutableList<EntitySOEsInternalDto>? = null,
    var additionalSegmentInternalDto: MutableList<AdditionalSegmentInternalDto>? = null
)
