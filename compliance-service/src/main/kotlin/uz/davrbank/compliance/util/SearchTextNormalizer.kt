package uz.davrbank.compliance.util

private val cyrillicToLatinMap = mapOf(
    "Ғ" to "G", "ғ" to "g",
    "Қ" to "Q", "қ" to "q",
    "Ҳ" to "H", "ҳ" to "h",
    "А" to "A", "а" to "a",
    "Б" to "B", "б" to "b",
    "В" to "V", "в" to "v",
    "Г" to "G", "г" to "g",
    "Д" to "D", "д" to "d",
    "Е" to "E", "е" to "e",
    "Ё" to "Yo", "ё" to "yo",
    "Ж" to "J", "ж" to "j",
    "З" to "Z", "з" to "z",
    "И" to "I", "и" to "i",
    "Й" to "Y", "й" to "y",
    "К" to "K", "к" to "k",
    "Л" to "L", "л" to "l",
    "М" to "M", "м" to "m",
    "Н" to "N", "н" to "n",
    "О" to "O", "о" to "o",
    "П" to "P", "п" to "p",
    "Р" to "R", "р" to "r",
    "С" to "S", "с" to "s",
    "Т" to "T", "т" to "t",
    "У" to "U", "у" to "u",
    "Ф" to "F", "ф" to "f",
    "Х" to "kh", "х" to "kh",
    "Ц" to "Ts", "ц" to "ts",
    "Ч" to "Ch", "ч" to "ch",
    "Ш" to "Sh", "ш" to "sh",
    "Щ" to "Shch", "щ" to "shch",
    "Ъ" to "'", "ъ" to "'",
    "Ы" to "I", "ы" to "i",
    "Ь" to "", "ь" to "",
    "Э" to "E", "э" to "e",
    "Ю" to "Yu", "ю" to "yu",
    "Я" to "Ya", "я" to "ya",
    "Ҷ" to "J", "ҷ" to "j",
    "Ӣ" to "I", "ӣ" to "i",
    "Ӯ" to "U", "ӯ" to "u",
    "№" to "N",
)

internal fun String.normalizeSearchText(): String =
    transliterateCyrillicToLatin()
        .trim()
        .replace(WHITESPACE_REGEX, " ")

internal fun String.normalizeOrganizationSearchText(): String {
    return normalizeSearchText()
}

internal fun String.transliterateCyrillicToLatin(): String {
    if (isBlank() || none { it in CYRILLIC_RANGE }) {
        return this
    }

    return buildString(length) {
        this@transliterateCyrillicToLatin.forEach { character ->
            append(cyrillicToLatinMap[character.toString()] ?: character)
        }
    }
}

private val CYRILLIC_RANGE = '\u0400'..'\u04FF'
private val WHITESPACE_REGEX = Regex("\\s+")
