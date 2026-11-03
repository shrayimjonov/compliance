package uz.davrbank.compliance.dto.request.filter

import com.fasterxml.jackson.annotation.JsonFormat
import uz.davrbank.compliance.enums.GoodsType
import uz.davrbank.compliance.enums.TransferComplianceStatus
import java.time.Instant
import java.util.UUID

data class TransferComplianceCheckListFilter(
    val status: TransferComplianceStatus? = null,
    val checkNumber: String? = null,
    val externalId: UUID? = null,
    val branchCode: String? = null,
    val contractTypeCode: String? = null,
    val contractNumber: String? = null,
    val contractIdn: String? = null,
    val clientName: String? = null,
    val clientTin: String? = null,
    val foreignPartnerName: String? = null,
    val foreignPartnerTin: String? = null,
    val currencyCode: String? = null,
    val goodsType: GoodsType? = null,
    val checkedBy: String? = null,
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    val startDate: Instant? = null,
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    val endDate: Instant? = null,
)
