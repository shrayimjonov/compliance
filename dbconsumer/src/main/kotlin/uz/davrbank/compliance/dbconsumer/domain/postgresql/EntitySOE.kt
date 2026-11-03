package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import java.util.Date

@Entity
class EntitySOE(
    @ManyToOne(fetch = FetchType.LAZY) var compliance: Compliance,
    @Id var id: String,
    var isActive: Boolean,
    var lastUpdated: Date
)
