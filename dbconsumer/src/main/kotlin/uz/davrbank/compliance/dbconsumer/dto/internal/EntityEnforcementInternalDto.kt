package uz.davrbank.compliance.dbconsumer.dto.internal

import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityEnforcement
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityEnforcementSubcategory

data class EntityEnforcementInternalDto(
    var entityEnforcement: EntityEnforcement,
    var entityEnforcementSubcategory: List<EntityEnforcementSubcategory>
)
