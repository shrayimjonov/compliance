package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import java.time.LocalDate
import java.util.Date

@Entity
class EntityDOB(
    @Id var id: String,
    var birthDate: LocalDate?,
    var lastUpdated: Date,
    @ManyToOne(fetch = FetchType.LAZY) var compliance: Compliance
)
