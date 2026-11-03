package uz.davrbank.compliance.controller.transfer

import jakarta.validation.Valid
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
import uz.davrbank.compliance.dto.request.filter.TransferComplianceCheckListFilter
import uz.davrbank.compliance.dto.request.filter.TransferComplianceCheckReportRequest
import uz.davrbank.compliance.dto.request.transfer.TransferComplianceCheckCreateRequest
import uz.davrbank.compliance.dto.request.transfer.TransferComplianceCheckStatusUpdateRequest
import uz.davrbank.compliance.dto.request.transfer.TransferComplianceCheckUpdateRequest
import uz.davrbank.compliance.dto.response.ResponseDTO
import uz.davrbank.compliance.dto.response.ResponsePageDTO
import uz.davrbank.compliance.dto.response.toResponsePageDTO
import uz.davrbank.compliance.dto.response.transfer.TransferComplianceCheckDetailDTO
import uz.davrbank.compliance.dto.response.transfer.TransferComplianceCheckResponse
import uz.davrbank.compliance.enums.LogSource
import uz.davrbank.compliance.service.ApiLogService
import uz.davrbank.compliance.service.TransferComplianceCheckService
import java.nio.charset.StandardCharsets
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

@Validated
@RestController
@RequestMapping(ApiConstants.TRANSFER_COMPLIANCE_CHECK_ROOT_API)
class TransferComplianceCheckController(
    private val transferComplianceCheckService: TransferComplianceCheckService,
    private val apiLogService: ApiLogService,
) {

    @PostMapping(ApiConstants.LIST)
    fun getList(
        @Valid @RequestBody filter: BaseFilter<TransferComplianceCheckListFilter>,
    ): ResponseEntity<ResponseDTO<ResponsePageDTO<TransferComplianceCheckResponse>>> = ResponseEntity.ok(
        ResponseDTO.ok(transferComplianceCheckService.getList(filter).toResponsePageDTO())
    )

    @PostMapping(ApiConstants.LIST + ApiConstants.EXTERNAL)
    fun getExternalList(
        @Valid @RequestBody filter: BaseFilter<TransferComplianceCheckListFilter>,
    ): ResponseEntity<ResponseDTO<ResponsePageDTO<TransferComplianceCheckResponse>>> = ResponseEntity.ok(
        ResponseDTO.ok(transferComplianceCheckService.getExternalList(filter).toResponsePageDTO())
    )

    @PostMapping(ApiConstants.REPORT + ApiConstants.DOWNLOAD)
    fun downloadReport(
        @Valid @RequestBody request: TransferComplianceCheckReportRequest,
    ): ResponseEntity<Resource> {
        val content = transferComplianceCheckService.downloadExternalReport(request)
        val contentDisposition = ContentDisposition.attachment()
            .filename(buildFileName(request), StandardCharsets.UTF_8)
            .build()

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
            .contentType(MediaType.parseMediaType(XLSX_CONTENT_TYPE))
            .contentLength(content.size.toLong())
            .body(ByteArrayResource(content))
    }

    private fun buildFileName(request: TransferComplianceCheckReportRequest): String {
        val params = request.params
        val parts = listOfNotNull(
            request.keyword,
            params?.checkNumber,
            params?.branchCode,
            params?.contractTypeCode,
            params?.contractNumber,
            params?.clientTin,
            params?.currencyCode,
            params?.goodsType?.name,
            params?.status?.name,
            params?.checkedBy,
            params?.externalId?.toString(),
            params?.startDate?.let(FILE_DATE_FORMATTER::format),
            params?.endDate?.let(FILE_DATE_FORMATTER::format),
        ).mapNotNull { it.sanitizeForFileName() }

        val suffix = if (parts.isEmpty()) FILE_DATE_FORMATTER.format(java.time.Instant.now()) else parts.joinToString("_")
        return "$FILE_NAME_PREFIX$suffix.xlsx"
    }

    private fun String.sanitizeForFileName(): String? =
        trim()
            .takeIf(String::isNotBlank)
            ?.replace(FILE_NAME_ILLEGAL_CHARS, "-")
            ?.trim('-')
            ?.takeIf(String::isNotBlank)

    @GetMapping(ApiConstants.EXTERNAL + ApiConstants.EXTERNAL_ID)
    fun getByExternalId(
        @PathVariable externalId: UUID,
    ): ResponseEntity<ResponseDTO<TransferComplianceCheckDetailDTO>> = ResponseEntity.ok(
        ResponseDTO.ok(transferComplianceCheckService.getByExternalId(externalId))
    )

    @PostMapping
    fun create(
        @Valid @RequestBody request: TransferComplianceCheckCreateRequest,
    ): ResponseEntity<ResponseDTO<TransferComplianceCheckResponse>> = ResponseEntity.status(HttpStatus.CREATED).body(
        ResponseDTO.ok(transferComplianceCheckService.create(request))
    )

    @PutMapping(ApiConstants.ID)
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: TransferComplianceCheckUpdateRequest,
    ): ResponseEntity<ResponseDTO<TransferComplianceCheckResponse>> = ResponseEntity.ok(
        ResponseDTO.ok(transferComplianceCheckService.update(id, request))
    )

    @PutMapping(ApiConstants.ID + ApiConstants.STATUS)
    fun updateStatusById(
        @PathVariable id: UUID,
        @Valid @RequestBody request: TransferComplianceCheckStatusUpdateRequest,
    ): ResponseEntity<ResponseDTO<TransferComplianceCheckResponse>> = ResponseEntity.ok(
        ResponseDTO.ok(transferComplianceCheckService.updateStatusById(id, request))
    )

    @PutMapping(ApiConstants.EXTERNAL + ApiConstants.EXTERNAL_ID + ApiConstants.STATUS)
    fun updateStatus(
        @PathVariable externalId: UUID,
        @Valid @RequestBody request: TransferComplianceCheckStatusUpdateRequest,
    ): ResponseEntity<ResponseDTO<TransferComplianceCheckResponse>> = ResponseEntity.ok(
        ResponseDTO.ok(transferComplianceCheckService.updateStatus(externalId, request))
    )

    @DeleteMapping(ApiConstants.ID)
    fun delete(
        @PathVariable id: UUID,
    ): ResponseEntity<ResponseDTO<UUID>> = ResponseEntity.ok(
        ResponseDTO.ok(transferComplianceCheckService.delete(id))
    )

    @GetMapping(ApiConstants.ID + ApiConstants.RESPONSE)
    fun getTifResponse(
        @PathVariable id: UUID,
    ): ResponseEntity<ResponseDTO<Map<String, Any>?>> = ResponseEntity.ok(
        ResponseDTO.ok(apiLogService.getResponseByRequestId(id.toString(), LogSource.TIF))
    )

    companion object {
        private const val XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        private const val FILE_NAME_PREFIX = "ТИФ_проверка_"
        private val FILE_NAME_ILLEGAL_CHARS = Regex("[\\\\/:*?\"<>|\\s]+")
        private val FILE_DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC)
    }
}
