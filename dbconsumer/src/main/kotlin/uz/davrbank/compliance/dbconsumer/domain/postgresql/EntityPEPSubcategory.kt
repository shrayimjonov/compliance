package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.*
import java.util.Date

@Entity
class EntityPEPSubcategory(
    @ManyToOne(fetch = FetchType.LAZY) var entityPep: EntityPEP,
    @Id var id: String,
    var pepSubcategoryDesc: String?,
    var subCategoryLabel: String?,
    var lastUpdated: Date?,
    @ManyToOne(fetch = FetchType.LAZY) var compliance: Compliance
)
