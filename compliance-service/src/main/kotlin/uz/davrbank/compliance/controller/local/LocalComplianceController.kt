package uz.davrbank.compliance.controller.local

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uz.davrbank.compliance.constants.ApiConstants
import uz.davrbank.compliance.dto.request.filter.BaseFilter
import uz.davrbank.compliance.dto.request.filter.LocalComplianceListFilter
import uz.davrbank.compliance.dto.request.local.ComplianceAdminRequest
import uz.davrbank.compliance.dto.response.MessageResponse
import uz.davrbank.compliance.dto.response.ResponseDTO
import uz.davrbank.compliance.dto.response.ResponsePageDTO
import uz.davrbank.compliance.dto.response.local.ComplianceAdminResponse
import uz.davrbank.compliance.dto.response.toResponsePageDTO
import uz.davrbank.compliance.service.LocalComplianceService

@Validated
@RestController
@RequestMapping(ApiConstants.LOCAL_COMPLIANCE_ROOT_API)
class LocalComplianceController(
    private val localComplianceService: LocalComplianceService,
) {

    @PostMapping(ApiConstants.LIST)
    fun getList(
        @Valid @RequestBody filter: BaseFilter<LocalComplianceListFilter>,
    ): ResponseEntity<ResponseDTO<ResponsePageDTO<ComplianceAdminResponse>>> = ResponseEntity.ok(
        ResponseDTO.ok(localComplianceService.getList(filter).toResponsePageDTO())
    )

    @GetMapping(ApiConstants.ID)
    fun get(
        @PathVariable @NotBlank id: String,
    ): ResponseEntity<ResponseDTO<ComplianceAdminResponse>> = ResponseEntity.ok(
        ResponseDTO.ok(localComplianceService.get(id))
    )

    @PostMapping
    fun create(
        @Valid @RequestBody request: ComplianceAdminRequest,
    ): ResponseEntity<ResponseDTO<ComplianceAdminResponse>> = ResponseEntity.status(HttpStatus.CREATED).body(
        ResponseDTO.ok(localComplianceService.create(request))
    )

    @PutMapping(ApiConstants.ID)
    fun update(
        @PathVariable @NotBlank id: String,
        @Valid @RequestBody request: ComplianceAdminRequest,
    ): ResponseEntity<ResponseDTO<ComplianceAdminResponse>> = ResponseEntity.ok(
        ResponseDTO.ok(localComplianceService.update(id, request))
    )

    @DeleteMapping(ApiConstants.ID)
    fun delete(
        @PathVariable @NotBlank id: String,
    ): ResponseEntity<ResponseDTO<MessageResponse>> = ResponseEntity.ok(
        ResponseDTO.ok(localComplianceService.delete(id))
    )
}
