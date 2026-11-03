package uz.davrbank.compliance.dto.request

data class OrganizationSearchQuery(
    val raw: String,
    val normalized: String,
    val tokens: List<String>,
    val filters: Map<String, String> = emptyMap(),
)
