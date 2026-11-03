package uz.davrbank.compliance.domain

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import uz.davrbank.compliance.enums.ApiLogStatus
import uz.davrbank.compliance.enums.LogSource
import java.time.Instant
import java.util.*


@Entity
@Table(name = "api_logs")
class ApiLog(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var requestId: String = UUID.randomUUID().toString(),

    @Column(columnDefinition = "TEXT")
    var username: String? = null,

    @Column(nullable = false, length = 16)
    var method: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var endpoint: String,

    @Column(columnDefinition = "TEXT")
    var queryParams: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var requestBody: Map<String, Any>? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var responseBody: Map<String, Any>? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var additional: Map<String, Any>? = null,

    @Column(nullable = false)
    var statusCode: Int,

    @Column(nullable = false)
    var timeTakenMs: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var status: ApiLogStatus = ApiLogStatus.CREATED,

    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var source: LogSource = LogSource.SMARTBAS,
) {

    @PrePersist
    fun prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now()
        }
    }
}
