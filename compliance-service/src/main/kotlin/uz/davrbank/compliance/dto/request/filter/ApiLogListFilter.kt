package uz.davrbank.compliance.dto.request.filter

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import uz.davrbank.compliance.enums.LogSource
import java.time.LocalDate

data class ApiLogListFilter(
    val method: String? = null,
    @field:JsonAlias("statuscode", "status_code")
    @field:Min(100)
    @field:Max(599)
    val statusCode: Int? = null,
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val createdDate: LocalDate? = null,
    @field:JsonIgnore
    @get:JsonIgnore
    val source: LogSource? = null,
)
