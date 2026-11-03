package uz.davrbank.compliance.dbconsumer.domain.postgresql

import jakarta.persistence.Embeddable
import java.util.Date

@Embeddable
data class ConsolidatedSanction(
    var consolidatedSanctionGUID: String?,
    var entityGUID: String?,
    var lastUpdated: Date?
)
