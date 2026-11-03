package uz.davrbank.compliance.dto.request.filter

data class OrgSearch(
    var orgType: OrgType? = OrgType.J,
    var region: String? = null,
    var birth: String? = null,
)
