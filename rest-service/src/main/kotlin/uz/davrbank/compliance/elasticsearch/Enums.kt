package uz.davrbank.compliance.elasticsearch

enum class SearchTarget(val field: String) {
    INDIVIDUAL("Individual"),
    ORGANIZATION("Organization")
}