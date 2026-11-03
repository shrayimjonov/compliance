package uz.davrbank.compliance.dto.request.transfer

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import uz.davrbank.compliance.enums.GoodsType
import uz.davrbank.compliance.enums.TransferComplianceStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class TransferComplianceCheckCreateRequest(
    @field:NotBlank
    val branchCode: String,

    @field:NotBlank
    val clientName: String,

    @field:NotBlank
    val contractTypeCode: String,

    @field:NotBlank
    val contractNumber: String,

    val clientTin: String? = null,
    val enterpriseRegistrationDate: LocalDate? = null,
    val activityType: String? = null,
    val contractDate: LocalDate? = null,
    val contractIdn: String? = null,
    val foreignPartnerName: String? = null,
    val foreignPartnerTin: String? = null,
    val directorAndRelatedParties: String? = null,
    val foreignPartnerCountryCode: String? = null,
    val foreignPartnerBank: String? = null,
    val bicSwift: String? = null,
    val foreignPartnerBankCountryCode: String? = null,
    val goodsType: GoodsType? = null,
    val goodsName: String? = null,
    val goodsTnvedCode: String? = null,
    val goodsOriginCountryCode: String? = null,
    val consignor: String? = null,
    val consignee: String? = null,
    val currencyCode: String? = null,
    val operationAmount: BigDecimal? = null,
    val lastOperationDate: LocalDate? = null,
    val feaOfficerFullName: String? = null,
    val decisionNote: String? = null,
    val fileIds: List<UUID>? = null,
)

data class TransferComplianceCheckUpdateRequest(
    val branchCode: String? = null,
    val clientName: String? = null,
    val contractTypeCode: String? = null,
    val contractNumber: String? = null,
    val clientTin: String? = null,
    val enterpriseRegistrationDate: LocalDate? = null,
    val activityType: String? = null,
    val contractDate: LocalDate? = null,
    val contractIdn: String? = null,
    val foreignPartnerName: String? = null,
    val foreignPartnerTin: String? = null,
    val directorAndRelatedParties: String? = null,
    val foreignPartnerCountryCode: String? = null,
    val foreignPartnerBank: String? = null,
    val bicSwift: String? = null,
    val foreignPartnerBankCountryCode: String? = null,
    val goodsType: GoodsType? = null,
    val goodsName: String? = null,
    val goodsTnvedCode: String? = null,
    val goodsOriginCountryCode: String? = null,
    val consignor: String? = null,
    val consignee: String? = null,
    val currencyCode: String? = null,
    val operationAmount: BigDecimal? = null,
    val lastOperationDate: LocalDate? = null,
    val feaOfficerFullName: String? = null,
    val decisionNote: String? = null,
    val fileIds: List<UUID>? = null,
)

data class TransferComplianceCheckStatusUpdateRequest(
    @field:NotNull
    val status: TransferComplianceStatus? = null,
    val rejectReason: String? = null,
)
