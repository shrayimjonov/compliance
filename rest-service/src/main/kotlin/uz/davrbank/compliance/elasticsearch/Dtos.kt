package uz.davrbank.compliance.elasticsearch

import com.fasterxml.jackson.annotation.JsonProperty

data class ESHits(
    val took: Long,
    val hits: Hits,
) {
    data class Hits(
        val total: Long,
        val maxScore: Double,
        val hits: List<Hit>
    )

    data class Hit(
        val score: Double,
        val firstname: String,
        val lastname: String,
        val extLookUpId: String,
        val id: String,
        val type: String,
        val dataType: String? = null,
    )
}



data class SearchResponse(
    val firstname: String,
    val lastname: String,
    val extLookUpId: String,
    val id: String,
    val type: String, // INDIVIDUAL // ORGANIZATION
    val complianceType: String, // entity yoki relationship tog'ridan to'gri yoki afillik asosida
    val dataType: String? = null, //LEXIS malumot qayerdan olinganligi
)

data class ElasticSearchResponse(
    val hits: HitsContainer
)


data class HitsContainer(
    val total: TotalHits,
    val hits: List<Hit>
)


data class TotalHits(
    val value: Long
)


data class Hit(
    @JsonProperty("_source") val source: SourceDoc,
    @JsonProperty("_score") val score: Double
)


data class SourceDoc(
    val id: String,
    val firstname: String?,
    val lastname: String?,
    val fullName: String?,
    val type: String,
    val complianceType: String,
    val dataType: String
)


data class ComplianceEntityDto(
    val id: String,
    val score: Double,
    val fullName: String?,
    val complianceType: String
)