package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import java.util.Date

@Entity
class EntityAdverseMedia(
    @Id var id: String,
    var adverseMediaDesc: String?,
    var lastUpdated: Date?,
    @ManyToOne(fetch = FetchType.LAZY) var compliance: Compliance
)
