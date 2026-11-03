package uz.davrbank.compliance.dto.response

import uz.davrbank.compliance.enums.LogSource
import uz.davrbank.compliance.enums.ApiLogStatus
import java.time.Instant
import java.util.UUID

data class ApiLogResponse(
    val id: UUID,
    val requestId: String,
    val username: String? = null,
    val method: String,
    val endpoint: String,
    val queryParams: String? = null,
    val requestBody: Map<String, Any>? = null,
    val responseListExists: Boolean = false,
    val additional: Map<String, Any>? = null,
    val status: ApiLogStatus = ApiLogStatus.CREATED,
    val statusCode: Int,
    val timeTakenMs: Long,
    val createdDate: Instant? = null,
    val source: LogSource = LogSource.NCI,
)
