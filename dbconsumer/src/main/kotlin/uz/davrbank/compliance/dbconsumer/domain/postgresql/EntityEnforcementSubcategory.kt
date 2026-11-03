package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import java.util.Date

@Entity
class EntityEnforcementSubcategory(
    @ManyToOne(fetch = FetchType.LAZY) var entityEnforcement: EntityEnforcement,
    @Id var id: String,
    var subCategoryLabel: String,
    var lastUpdated: Date,
    @ManyToOne var compliance: Compliance
)
