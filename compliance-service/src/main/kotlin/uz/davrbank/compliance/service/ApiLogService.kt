package uz.davrbank.compliance.service

import org.springframework.data.domain.Page
import uz.davrbank.compliance.domain.ApiLog
import uz.davrbank.compliance.dto.request.filter.ApiLogListFilter
import uz.davrbank.compliance.dto.request.filter.BaseFilter
import uz.davrbank.compliance.dto.response.ApiLogResponse
import uz.davrbank.compliance.enums.LogSource
import java.util.UUID

interface ApiLogService {
    fun save(log: ApiLog)

    fun save(event: ApiLogEvent)

    fun getList(filter: BaseFilter<ApiLogListFilter>): Page<ApiLogResponse>

    fun getResponse(id: UUID): Map<String, Any>?

    fun getResponse(id: UUID, source: LogSource): Map<String, Any>? = getResponse(id)

    fun getResponseByRequestId(requestId: String, source: LogSource): Map<String, Any>?

    fun existingResponseRequestIds(requestIds: Collection<String>, source: LogSource): Set<String>

    fun updateStatus(id: UUID): ApiLogResponse
}

data class ApiLogEvent(
    val requestId: String,
    val username: String?,
    val method: String,
    val endpoint: String,
    val queryParams: String?,
    val requestBody: Any?,
    val responseBody: Any?,
    val source: LogSource,
    val statusCode: Int,
    val timeTakenMs: Long,
)