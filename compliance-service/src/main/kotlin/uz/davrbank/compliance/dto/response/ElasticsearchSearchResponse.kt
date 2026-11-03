package uz.davrbank.compliance.dto.response

data class ElasticsearchSearchResponse(
    val took: Long,
    val total: Long,
    val maxScore: Double,
    val page: Int,
    val size: Int,
    val list: List<ElasticsearchSearchHit>,
)

data class ElasticsearchSearchHit(
    val id: String,
    val score: Double,
    val name: String?,
    val fullName: String?,
    val firstname: String?,
    val lastname: String?,
    val identificationNumber: String?,
    val region: String?,
    val birth: String?,
    val entityGuid: String?,
    val entityAdverseMedia: Boolean,
    val extLookUpId: String?,
    val type: String?,
    val complianceType: String?,
    val dataType: String?,
)
