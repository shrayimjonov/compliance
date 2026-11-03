package uz.davrbank.compliance.dto.request.abs

import jakarta.validation.constraints.NotNull
import uz.davrbank.compliance.enums.AbsCheckOperationType
import uz.davrbank.compliance.enums.AbsComplianceStatus
import uz.davrbank.compliance.enums.AbsRequestType
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Date

data class AbsCheckerRequest(
    val isResident: Boolean,
    val firstname: String? = null,
    val lastname: String? = null,
    val patronymic: String? = null,
    val gender: String? = null,
    val birthday: LocalDate? = null,
    val nationality: String? = null,
    @field:NotNull
    val type: AbsRequestType? = null,
    val clientInn: String? = null,
    val orgName: String? = null,
    val country: String? = null,
    @field:NotNull
    val operationType: AbsCheckOperationType? = null,
    val swiftCode: String? = null,
    val bankBik: String? = null,
    val address: String? = null,
    val director: String? = null,
    val directorInn: String? = null,
    val founder: String? = null,
    val founderInn: String? = null,
    val partnerName: String? = null,
    val partnerInn: String? = null,
    val orgRegistrationDate: Date? = null,
    val participationShare: BigDecimal? = null,
    val branch: String? = null,
    val actionType: String? = null,
    val requestedDate: Date? = null,
    val comment: String? = null,
)

data class AbsCheckerChangeStatusRequest(
    @field:NotNull
    val status: AbsComplianceStatus? = null,
    val comment: String? = null,
)
