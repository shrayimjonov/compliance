package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import java.util.Date

@Entity
class EntityAlias(
    @Id var id: String,
    val aliasTypeDesc: String?,
    val englishDescription: String?,
    val firstName: String?,
    val lastName: String?,
    val lastUpdated: Date?,
    val middleName: String?,
    val name: String?,
    val prefix: String?,
    val suffix: String?,
    @ManyToOne(fetch = FetchType.LAZY) var compliance: Compliance
)
