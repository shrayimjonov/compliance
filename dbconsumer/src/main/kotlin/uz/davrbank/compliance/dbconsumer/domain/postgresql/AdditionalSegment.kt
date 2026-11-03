package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import java.util.Date

@Entity
class AdditionalSegment(
    @ManyToOne(fetch = FetchType.LAZY) var compliance: Compliance,
    var type: String?,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    var createdDate: Date? = null,
    var deleted: Boolean = false,
)
