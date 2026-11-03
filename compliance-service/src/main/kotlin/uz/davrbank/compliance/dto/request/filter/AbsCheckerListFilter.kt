package uz.davrbank.compliance.dto.request.filter

import com.fasterxml.jackson.annotation.JsonFormat
import uz.davrbank.compliance.enums.AbsCheckOperationType
import uz.davrbank.compliance.enums.AbsComplianceStatus
import java.time.LocalDateTime

data class AbsCheckerListFilter(
    val status: AbsComplianceStatus? = null,
    val operationType: AbsCheckOperationType? = null,
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    val startDate: LocalDateTime? = null,
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    val endDate: LocalDateTime? = null,
    val isResident: Boolean? = null,
)
