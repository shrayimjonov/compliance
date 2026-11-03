package uz.davrbank.compliance.controller.apilog

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import uz.davrbank.compliance.constants.ApiConstants
import uz.davrbank.compliance.dto.request.filter.ApiLogListFilter
import uz.davrbank.compliance.dto.request.filter.BaseFilter
import uz.davrbank.compliance.dto.response.ApiLogResponse
import uz.davrbank.compliance.dto.response.ResponseDTO
import uz.davrbank.compliance.dto.response.ResponsePageDTO
import uz.davrbank.compliance.dto.response.toResponsePageDTO
import uz.davrbank.compliance.enums.LogSource
import uz.davrbank.compliance.service.ApiLogService
import java.util.*

@Validated
@RestController
@RequestMapping(ApiConstants.API_LOG_ROOT_API)
class ApiLogController(
    private val apiLogService: ApiLogService,
) {

    @PostMapping(ApiConstants.LIST)
    fun getList(
        @Valid @RequestBody filter: BaseFilter<ApiLogListFilter>,
    ): ResponseEntity<ResponseDTO<ResponsePageDTO<ApiLogResponse>>> = ResponseEntity.ok(
        ResponseDTO.ok(apiLogService.getList(filter.withNciSource()).toResponsePageDTO())
    )

    @GetMapping(ApiConstants.ID + ApiConstants.RESPONSE)
    fun getResponse(
        @PathVariable id: UUID,
    ): ResponseEntity<ResponseDTO<Map<String, Any>?>> = ResponseEntity.ok(
        ResponseDTO.ok(apiLogService.getResponse(id, LogSource.NCI))
    )

    @PutMapping(ApiConstants.ID + ApiConstants.STATUS)
    fun updateStatus(
        @PathVariable id: UUID,
    ): ResponseEntity<ResponseDTO<ApiLogResponse>> = ResponseEntity.ok(
        ResponseDTO.ok(apiLogService.updateStatus(id))
    )

    private fun BaseFilter<ApiLogListFilter>.withNciSource(): BaseFilter<ApiLogListFilter> = BaseFilter(
        requestId = requestId,
        keyword = keyword,
        params = (params ?: ApiLogListFilter()).copy(source = LogSource.NCI),
        pagination = pagination,
    )
}
