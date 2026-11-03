package uz.davrbank.compliance.dbconsumer.dto.internal

import uz.davrbank.compliance.dbconsumer.domain.postgresql.Compliance
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityAddress
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityAlias
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityCountryAssociation
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityDOB
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityIdentification
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityPosition
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityRelationship
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityRemark
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntitySourceItem

data class AdditionalDataToEntityDto(
    val entityDOBs: MutableList<EntityDOB>,
    val entityAddresses: MutableList<EntityAddress>,
    val entityCountryAssociations: MutableList<EntityCountryAssociation>,
    val entityPositions: MutableList<EntityPosition>,
    val entityRelationships: MutableList<EntityRelationship>,
    val entityRemarks: MutableList<EntityRemark>,
    val entityAlias: MutableList<EntityAlias>,
    val entityIdentification: MutableList<EntityIdentification>,
    val entitySourceItem: MutableList<EntitySourceItem>,
    val entitySegment: EntitySegmentDto,
    val entityComplianceEntity: Compliance
)
