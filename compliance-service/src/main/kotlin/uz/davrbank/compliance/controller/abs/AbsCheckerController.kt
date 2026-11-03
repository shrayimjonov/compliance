package uz.davrbank.compliance.controller.abs

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import uz.davrbank.compliance.constants.ApiConstants
import uz.davrbank.compliance.dto.request.abs.AbsCheckerChangeStatusRequest
import uz.davrbank.compliance.dto.request.abs.AbsCheckerRequest
import uz.davrbank.compliance.dto.request.filter.AbsCheckerListFilter
import uz.davrbank.compliance.dto.request.filter.BaseFilter
import uz.davrbank.compliance.dto.response.ResponseDTO
import uz.davrbank.compliance.dto.response.ResponsePageDTO
import uz.davrbank.compliance.dto.response.abs.*
import uz.davrbank.compliance.dto.response.toResponsePageDTO
import uz.davrbank.compliance.exception.BadRequestException
import uz.davrbank.compliance.service.AbsCheckerService

@Validated
@RestController
@RequestMapping(ApiConstants.ABS_CHECKER_ROOT_API)
class AbsCheckerController(
    private val absCheckerService: AbsCheckerService,
) {

    @PostMapping(ApiConstants.LIST)
    fun getList(
        @Valid @RequestBody filter: BaseFilter<AbsCheckerListFilter>,
    ): ResponseEntity<ResponseDTO<ResponsePageDTO<AbsCheckerAdminResponse>>> = ResponseEntity.ok(
        ResponseDTO.ok(
            absCheckerService.getList(filter).toResponsePageDTO()
        )
    )

    @GetMapping(ApiConstants.ID)
    fun get(
        @PathVariable @NotBlank id: String,
    ): ResponseEntity<ResponseDTO<AbsCheckerAdminGetOneResponse>> = ResponseEntity.ok(
        ResponseDTO.ok(absCheckerService.get(id))
    )

    @GetMapping(ApiConstants.ID + ApiConstants.RESPONSE)
    fun getResponse(
        @PathVariable @NotBlank id: String,
        pageable: Pageable,
    ): ResponseEntity<ResponseDTO<AbsCheckerSearchResponse?>> = ResponseEntity.ok(
        ResponseDTO.ok(absCheckerService.getResponse(id, pageable))
    )

    @PostMapping(ApiConstants.CHECK_ACCOUNT)
    fun checkAccount(
        @Valid @RequestBody request: AbsCheckerRequest,
    ): ResponseEntity<ResponseDTO<AbsCheckerResponse>> = ResponseEntity.status(HttpStatus.CREATED).body(
        ResponseDTO.ok(absCheckerService.create(request))
    )

    @GetMapping(ApiConstants.CHECK_STATUS)
    fun checkOpenAccountRequestStatus(
        @RequestParam @NotBlank id: String,
    ): ResponseEntity<ResponseDTO<AbsCheckerAdminGetOneResponse>> = ResponseEntity.ok(
        ResponseDTO.ok(absCheckerService.checkStatus(id.trimToRequired("id")))
    )

    @GetMapping(ApiConstants.CHECK_STATUS + "/bf" + ApiConstants.ID)
    fun checkOpenAccountRequestStatusForBf(
        @PathVariable @NotBlank id: String,
    ): ResponseEntity<CheckResultResponseWrapper> {
        val response = absCheckerService.checkStatus(id.trimToRequired("id"))
        val responseId =
            response.id ?: return ResponseEntity.ok(CheckResultResponseWrapper(status = false))
        val status = response.complianceStatus ?: return ResponseEntity.ok(
            CheckResultResponseWrapper(
                status = false
            )
        )

        return ResponseEntity.ok(
            CheckResultResponseWrapper(
                status = true,
                data = AbsDataCheckerResultResponse(
                    id = responseId,
                    status = status,
                    comment = response.comment,
                )
            )
        )
    }

    @PutMapping("${ApiConstants.CHANGE_STATUS}${ApiConstants.ID}")
    fun updateStatus(
        @PathVariable @NotBlank id: String,
        @Valid @RequestBody request: AbsCheckerChangeStatusRequest,
    ): ResponseEntity<ResponseDTO<AbsCheckerAdminResponse>> = ResponseEntity.ok(
        ResponseDTO.ok(absCheckerService.updateStatus(id, request))
    )

    private fun String.trimToRequired(fieldName: String): String =
        trim().takeIf(String::isNotBlank)
            ?: throw BadRequestException("Property '$fieldName' must be provided.")
}
