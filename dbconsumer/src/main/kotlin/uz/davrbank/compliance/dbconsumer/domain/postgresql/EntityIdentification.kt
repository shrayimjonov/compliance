package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import java.time.LocalDate
import java.util.Date

@Entity
class EntityIdentification(
    @Id var id: String,
    val administrativeUnitName: String?,
    val expiration: LocalDate?,
    val isoStandard: String?,
    val identificationIssuer: String?,
    val identificationNumber: String?,
    val identificationTypeDesc: String?,
    val issueDate: LocalDate?,
    val lastUpdated: Date?,
    @ManyToOne(fetch = FetchType.LAZY) var compliance: Compliance
)
