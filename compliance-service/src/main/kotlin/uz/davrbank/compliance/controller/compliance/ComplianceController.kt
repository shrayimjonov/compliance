package uz.davrbank.compliance.controller.compliance

import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uz.davrbank.compliance.constants.ApiConstants
import uz.davrbank.compliance.dto.response.*
import uz.davrbank.compliance.service.ComplianceDetailService

@Validated
@RestController
@RequestMapping(ApiConstants.COMPLIANCE_ROOT_API)
class ComplianceController(
    private val complianceDetailService: ComplianceDetailService,
) {

    @Deprecated("Use getCompliance instead")
    @GetMapping(ApiConstants.ID)
    fun getComplianceDetails(
        @PathVariable @NotBlank id: String,
    ): ResponseEntity<ResponseDTO<ComplianceDetailsDto>> = ResponseEntity.ok(
        ResponseDTO.ok(complianceDetailService.getComplianceDetails(id))
    )

    @GetMapping("/{complianceId}/compliance")
    fun getCompliance(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<ComplianceDetailDto>> = ok(
        complianceDetailService.getCompliance(complianceId)
    )

    @GetMapping("/{complianceId}/entity-dobs")
    fun getEntityDobs(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<EntityDobDetailDto>>> = ok(
        complianceDetailService.getEntityDobs(complianceId)
    )

    @GetMapping("/{complianceId}/entity-addresses")
    fun getEntityAddresses(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<EntityAddressDetailDto>>> = ok(
        complianceDetailService.getEntityAddresses(complianceId)
    )

    @GetMapping("/{complianceId}/entity-aliases")
    fun getEntityAliases(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<EntityAliasDetailDto>>> = ok(
        complianceDetailService.getEntityAliases(complianceId)
    )

    @GetMapping("/{complianceId}/entity-identifications")
    fun getEntityIdentifications(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<EntityIdentificationDetailDto>>> = ok(
        complianceDetailService.getEntityIdentifications(complianceId)
    )

    @GetMapping("/{complianceId}/entity-country-associations")
    fun getEntityCountryAssociations(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<EntityCountryAssociationDetailDto>>> = ok(
        complianceDetailService.getEntityCountryAssociations(complianceId)
    )

    @GetMapping("/{complianceId}/entity-positions")
    fun getEntityPositions(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<EntityPositionDetailDto>>> = ok(
        complianceDetailService.getEntityPositions(complianceId)
    )

    @GetMapping("/{complianceId}/entity-relationships")
    fun getEntityRelationships(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<EntityRelationshipDetailDto>>> = ok(
        complianceDetailService.getEntityRelationships(complianceId)
    )

    @GetMapping("/{complianceId}/entity-remarks")
    fun getEntityRemarks(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<EntityRemarkDetailDto>>> = ok(
        complianceDetailService.getEntityRemarks(complianceId)
    )

    @GetMapping("/{complianceId}/entity-source-items")
    fun getEntitySourceItems(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<EntitySourceItemDetailDto>>> = ok(
        complianceDetailService.getEntitySourceItems(complianceId)
    )

    @GetMapping("/{complianceId}/entity-segment")
    fun getEntitySegment(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<EntitySegmentDetailDto?>> = ok(
        complianceDetailService.getEntitySegment(complianceId)
    )

    @GetMapping("/{complianceId}/entity-peps")
    fun getEntityPeps(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<EntityPepDetailDto>>> = ok(
        complianceDetailService.getEntityPeps(complianceId)
    )

    @GetMapping("/{complianceId}/entity-pep-subcategories")
    fun getEntityPepSubcategories(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<EntityPepSubcategoryDetailDto>>> = ok(
        complianceDetailService.getEntityPepSubcategories(complianceId)
    )

    @GetMapping("/{complianceId}/entity-adverse-media")
    fun getEntityAdverseMedia(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<EntityAdverseMediaDetailDto>>> = ok(
        complianceDetailService.getEntityAdverseMedia(complianceId)
    )

    @GetMapping("/{complianceId}/entity-adverse-media-subcategories")
    fun getEntityAdverseMediaSubcategories(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<EntityAdverseMediaSubcategoryDetailDto>>> = ok(
        complianceDetailService.getEntityAdverseMediaSubcategories(complianceId)
    )

    @GetMapping("/{complianceId}/entity-enforcements")
    fun getEntityEnforcements(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<EntityEnforcementDetailDto>>> = ok(
        complianceDetailService.getEntityEnforcements(complianceId)
    )

    @GetMapping("/{complianceId}/entity-enforcement-subcategories")
    fun getEntityEnforcementSubcategories(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<EntityEnforcementSubcategoryDetailDto>>> = ok(
        complianceDetailService.getEntityEnforcementSubcategories(complianceId)
    )

    @GetMapping("/{complianceId}/entity-sanctions")
    fun getEntitySanctions(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<EntitySanctionDetailDto>>> = ok(
        complianceDetailService.getEntitySanctions(complianceId)
    )

    @GetMapping("/{complianceId}/entity-soes")
    fun getEntitySoes(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<EntitySoeDetailDto>>> = ok(
        complianceDetailService.getEntitySoes(complianceId)
    )

    @GetMapping("/{complianceId}/entity-soe-subcategories")
    fun getEntitySoeSubcategories(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<EntitySoeSubcategoryDetailDto>>> = ok(
        complianceDetailService.getEntitySoeSubcategories(complianceId)
    )

    @GetMapping("/{complianceId}/entity-soe-domains")
    fun getEntitySoeDomains(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<EntitySoeDomainDetailDto>>> = ok(
        complianceDetailService.getEntitySoeDomains(complianceId)
    )

    @GetMapping("/{complianceId}/additional-segments")
    fun getAdditionalSegments(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<AdditionalSegmentDetailDto>>> = ok(
        complianceDetailService.getAdditionalSegments(complianceId)
    )

    @GetMapping("/{complianceId}/records")
    fun getRecords(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<RecordDetailDto>>> = ok(
        complianceDetailService.getRecords(complianceId)
    )

    @GetMapping("/{complianceId}/fields")
    fun getFields(
        @PathVariable @NotBlank complianceId: String,
    ): ResponseEntity<ResponseDTO<List<FieldDetailDto>>> = ok(
        complianceDetailService.getFields(complianceId)
    )

    private fun <T> ok(data: T): ResponseEntity<ResponseDTO<T>> = ResponseEntity.ok(ResponseDTO.ok(data))
}
