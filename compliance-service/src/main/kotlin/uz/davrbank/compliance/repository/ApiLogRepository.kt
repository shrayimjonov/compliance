package uz.davrbank.compliance.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uz.davrbank.compliance.domain.ApiLog
import uz.davrbank.compliance.enums.LogSource
import java.util.UUID

interface ApiLogRepository : JpaRepository<ApiLog, UUID>, JpaSpecificationExecutor<ApiLog> {
    fun findByIdAndSource(id: UUID, source: LogSource): ApiLog?

    fun findFirstByRequestIdAndSourceOrderByCreatedAtDesc(requestId: String, source: LogSource): ApiLog?

    @Query(
        value = """
            SELECT DISTINCT a.request_id
            FROM api_logs a
            WHERE a.source = :source
              AND a.request_id IN (:requestIds)
              AND EXISTS (
                  SELECT 1
                  FROM jsonb_each(a.response_body) AS entry(search_key, search_value)
                  WHERE jsonb_typeof(entry.search_value -> 'list') = 'array'
                    AND jsonb_array_length(entry.search_value -> 'list') > 0
              )
        """,
        nativeQuery = true,
    )
    fun findRequestIdsWithResults(
        @Param("requestIds") requestIds: Collection<String>,
        @Param("source") source: String,
    ): List<String>
}
