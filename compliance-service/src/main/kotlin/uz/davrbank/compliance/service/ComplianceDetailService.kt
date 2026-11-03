package uz.davrbank.compliance.service

import uz.davrbank.compliance.dto.response.AdditionalSegmentDetailDto
import uz.davrbank.compliance.dto.response.ComplianceDetailsDto
import uz.davrbank.compliance.dto.response.ComplianceDetailDto
import uz.davrbank.compliance.dto.response.EntityAddressDetailDto
import uz.davrbank.compliance.dto.response.EntityAdverseMediaDetailDto
import uz.davrbank.compliance.dto.response.EntityAdverseMediaSubcategoryDetailDto
import uz.davrbank.compliance.dto.response.EntityAliasDetailDto
import uz.davrbank.compliance.dto.response.EntityCountryAssociationDetailDto
import uz.davrbank.compliance.dto.response.EntityDobDetailDto
import uz.davrbank.compliance.dto.response.EntityEnforcementDetailDto
import uz.davrbank.compliance.dto.response.EntityEnforcementSubcategoryDetailDto
import uz.davrbank.compliance.dto.response.EntityIdentificationDetailDto
import uz.davrbank.compliance.dto.response.EntityPepDetailDto
import uz.davrbank.compliance.dto.response.EntityPepSubcategoryDetailDto
import uz.davrbank.compliance.dto.response.EntityPositionDetailDto
import uz.davrbank.compliance.dto.response.EntityRelationshipDetailDto
import uz.davrbank.compliance.dto.response.EntityRemarkDetailDto
import uz.davrbank.compliance.dto.response.EntitySanctionDetailDto
import uz.davrbank.compliance.dto.response.EntitySegmentDetailDto
import uz.davrbank.compliance.dto.response.EntitySoeDetailDto
import uz.davrbank.compliance.dto.response.EntitySoeDomainDetailDto
import uz.davrbank.compliance.dto.response.EntitySoeSubcategoryDetailDto
import uz.davrbank.compliance.dto.response.EntitySourceItemDetailDto
import uz.davrbank.compliance.dto.response.FieldDetailDto
import uz.davrbank.compliance.dto.response.RecordDetailDto

interface ComplianceDetailService {
    fun getComplianceDetails(complianceId: String): ComplianceDetailsDto

    fun getCompliance(complianceId: String): ComplianceDetailDto

    fun getEntityDobs(complianceId: String): List<EntityDobDetailDto>

    fun getEntityAddresses(complianceId: String): List<EntityAddressDetailDto>

    fun getEntityAliases(complianceId: String): List<EntityAliasDetailDto>

    fun getEntityIdentifications(complianceId: String): List<EntityIdentificationDetailDto>

    fun getEntityCountryAssociations(complianceId: String): List<EntityCountryAssociationDetailDto>

    fun getEntityPositions(complianceId: String): List<EntityPositionDetailDto>

    fun getEntityRelationships(complianceId: String): List<EntityRelationshipDetailDto>

    fun getEntityRemarks(complianceId: String): List<EntityRemarkDetailDto>

    fun getEntitySourceItems(complianceId: String): List<EntitySourceItemDetailDto>

    fun getEntitySegment(complianceId: String): EntitySegmentDetailDto?

    fun getEntityPeps(complianceId: String): List<EntityPepDetailDto>

    fun getEntityPepSubcategories(complianceId: String): List<EntityPepSubcategoryDetailDto>

    fun getEntityAdverseMedia(complianceId: String): List<EntityAdverseMediaDetailDto>

    fun getEntityAdverseMediaSubcategories(complianceId: String): List<EntityAdverseMediaSubcategoryDetailDto>

    fun getEntityEnforcements(complianceId: String): List<EntityEnforcementDetailDto>

    fun getEntityEnforcementSubcategories(complianceId: String): List<EntityEnforcementSubcategoryDetailDto>

    fun getEntitySanctions(complianceId: String): List<EntitySanctionDetailDto>

    fun getEntitySoes(complianceId: String): List<EntitySoeDetailDto>

    fun getEntitySoeSubcategories(complianceId: String): List<EntitySoeSubcategoryDetailDto>

    fun getEntitySoeDomains(complianceId: String): List<EntitySoeDomainDetailDto>

    fun getAdditionalSegments(complianceId: String): List<AdditionalSegmentDetailDto>

    fun getRecords(complianceId: String): List<RecordDetailDto>

    fun getFields(complianceId: String): List<FieldDetailDto>
}
