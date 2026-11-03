package uz.davrbank.compliance.elasticsearch

import co.elastic.clients.elasticsearch.core.SearchResponse
import uz.davrbank.compliance.elasticsearch.HitKeys.COMPLIANCE_TYPE
import uz.davrbank.compliance.elasticsearch.HitKeys.DATA_TYPE
import uz.davrbank.compliance.elasticsearch.HitKeys.EXT_LOOK_UP_ID
import uz.davrbank.compliance.elasticsearch.HitKeys.FIRSTNAME
import uz.davrbank.compliance.elasticsearch.HitKeys.LASTNAME
import uz.davrbank.compliance.risk.ComplianceResponse


fun SearchResponse<Map<*, *>>.toESHits(): ESHits {
    val hits = this.hits().hits().map { hit ->
        val score = hit.score() ?: 0.0
        val firstname = hit.source()?.get(FIRSTNAME) ?: ""
        val lastname = hit.source()?.get(LASTNAME) ?: ""
        val extLookUpId = hit.source()?.get(EXT_LOOK_UP_ID) ?: ""
        val type = hit.source()?.get(COMPLIANCE_TYPE) ?: ""
        val dataType = hit.source()?.get(DATA_TYPE)
        ESHits.Hit(
            score,
            firstname.toString(),
            lastname.toString(),
            extLookUpId.toString(),
            hit.id(),
            type.toString(),
            dataType.toString(),
        )
    }
    return ESHits(
        took(),
        ESHits.Hits(
            hits().total()?.value() ?: 0,
            hits().maxScore() ?: 0.0,
            hits
        )
    )
}

fun ESHits.toResponse() = ComplianceResponse(this.hits.maxScore)

object HitKeys {
    const val FIRSTNAME = "firstname"
    const val LASTNAME = "lastname"
    const val POSTGRES_ID = "postgresId"
    const val EXT_LOOK_UP_ID = "extLookUpId"
    const val COMPLIANCE_TYPE = "complianceType"
    const val DATA_TYPE = "dataType"
}
