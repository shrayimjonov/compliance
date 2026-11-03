package uz.davrbank.compliance.enums

enum class Language {
    UZB,
    RU;

    companion object {
        fun fromValueOrNull(value: String?): Language? =
            value?.trim()
                ?.takeIf(String::isNotBlank)
                ?.let { normalized ->
                    entries.firstOrNull { language -> language.name.equals(normalized, ignoreCase = true) }
                }
    }
}
