package uz.davrbank.compliance.util

import org.springframework.stereotype.Component
import java.text.Normalizer

@Component
class TextNormalizer {

    fun normalize(input: String): String = Normalizer.normalize(input.normalizeSearchText(), Normalizer.Form.NFD)
        .replace(COMBINING_MARKS_REGEX, "")
        .lowercase()
        .replace(NON_ALPHANUMERIC_REGEX, " ")
        .replace(WHITESPACE_REGEX, " ")
        .trim()

    fun tokenize(input: String): List<String> = normalize(input)
        .split(WHITESPACE_REGEX)
        .asSequence()
        .filter { it.length >= MIN_TOKEN_LENGTH }
        .distinct()
        .toList()

    fun compact(input: String): String = normalize(input)
        .replace(" ", "")

    fun compactOrganization(input: String): String = compact(input.normalizeOrganizationSearchText())

    companion object {
        private const val MIN_TOKEN_LENGTH = 3

        private val COMBINING_MARKS_REGEX = Regex("\\p{Mn}+")
        private val NON_ALPHANUMERIC_REGEX = Regex("[^a-z0-9\\s]")
        private val WHITESPACE_REGEX = Regex("\\s+")
    }
}
