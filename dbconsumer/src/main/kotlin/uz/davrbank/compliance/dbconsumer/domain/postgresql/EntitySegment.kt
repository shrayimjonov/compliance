package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import java.util.Date

@Entity
class EntitySegment(
    var entityPep: Boolean = false,
    var entityAdverseMedia: Boolean = false,
    var entityEnforcement: Boolean = false,
    var entitySanction: Boolean = false,
    var entitySOE: Boolean = false,
    var additionalSegment: Boolean = false,
    @ManyToOne(fetch = FetchType.LAZY) var compliance: Compliance,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    var createdDate: Date? = null,
    var deleted: Boolean = false,
)
