package uz.davrbank.compliance.service

import org.springframework.data.domain.Page
import uz.davrbank.compliance.dto.request.filter.BaseFilter
import uz.davrbank.compliance.dto.request.filter.TransferComplianceCheckListFilter
import uz.davrbank.compliance.dto.request.filter.TransferComplianceCheckReportRequest
import uz.davrbank.compliance.dto.request.transfer.TransferComplianceCheckCreateRequest
import uz.davrbank.compliance.dto.request.transfer.TransferComplianceCheckStatusUpdateRequest
import uz.davrbank.compliance.dto.request.transfer.TransferComplianceCheckUpdateRequest
import uz.davrbank.compliance.dto.response.transfer.TransferComplianceCheckDetailDTO
import uz.davrbank.compliance.dto.response.transfer.TransferComplianceCheckResponse
import java.util.UUID

interface TransferComplianceCheckService {

    fun getList(filter: BaseFilter<TransferComplianceCheckListFilter>): Page<TransferComplianceCheckResponse>

    fun getExternalList(filter: BaseFilter<TransferComplianceCheckListFilter>): Page<TransferComplianceCheckResponse>

    fun downloadExternalReport(request: TransferComplianceCheckReportRequest): ByteArray

    fun delete(id: UUID): UUID

    fun getByExternalId(externalId: UUID): TransferComplianceCheckDetailDTO

    fun create(request: TransferComplianceCheckCreateRequest): TransferComplianceCheckResponse

    fun update(id: UUID, request: TransferComplianceCheckUpdateRequest): TransferComplianceCheckResponse

    fun updateStatusById(
        id: UUID,
        request: TransferComplianceCheckStatusUpdateRequest,
    ): TransferComplianceCheckResponse

    fun updateStatus(
        externalId: UUID,
        request: TransferComplianceCheckStatusUpdateRequest,
    ): TransferComplianceCheckResponse
}
