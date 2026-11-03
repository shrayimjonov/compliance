package uz.davrbank.compliance.dbconsumer.util

import kotlin.text.iterator

fun convertCyrillicToLatin(text: String?, defaultValue: String = ""): String {
    return try {
        if (text.isNullOrBlank()) return defaultValue

        val hasCyrillic = text.any { it in '\u0400'..'\u04FF' }
        if (!hasCyrillic) return text

        val cyrillicToLatinMap = mapOf(
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
            "№" to "N"
        )

        val result = StringBuilder()
        for (char in text) {
            result.append(cyrillicToLatinMap[char.toString()] ?: char)
        }
        val converted = result.toString().trim()
        if (converted.isBlank()) defaultValue else converted.lowercase()
    } catch (e: Exception) {
        e.printStackTrace()
        defaultValue
    }
}
