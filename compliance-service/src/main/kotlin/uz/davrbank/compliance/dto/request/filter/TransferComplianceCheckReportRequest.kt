package uz.davrbank.compliance.dto.request.filter

import jakarta.validation.Valid

data class TransferComplianceCheckReportRequest(
    val requestId: String? = null,
    val keyword: String? = null,
    @field:Valid
    val params: TransferComplianceCheckListFilter? = null,
)
