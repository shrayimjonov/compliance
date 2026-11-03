package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import java.util.Date

@Entity
class EntitySOEDomain(
    @ManyToOne(fetch = FetchType.LAZY) var entitySOE: EntitySOE,
    @Id var id: String,
    val soeDomainDesc: String,
    val lastUpdated: Date,
    @ManyToOne(fetch = FetchType.LAZY) val compliance: Compliance
)
