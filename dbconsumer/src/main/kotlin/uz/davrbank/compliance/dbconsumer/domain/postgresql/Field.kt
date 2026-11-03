package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import java.util.Date

@Entity
class Field(
    @ManyToOne(fetch = FetchType.LAZY) val record: Record,
    val derivedName: String?,
    val derivedValue: String?,
    @ManyToOne(fetch = FetchType.LAZY) val compliance: Compliance,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    var createdDate: Date? = null,
    var deleted: Boolean = false,
)
