package uz.davrbank.compliance

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import uz.davrbank.compliance.CharUtils.ES_DROP_CHARS
import uz.davrbank.compliance.backup.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

fun Date.plus(amount: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): Date {
    val add = when (unit) {
        TimeUnit.DAYS -> amount * 24 * 60 * 60 * 1000
        TimeUnit.HOURS -> amount * 60 * 60 * 1000
        TimeUnit.MINUTES -> amount * 60 * 1000
        TimeUnit.SECONDS -> amount * 1000
        else -> amount
    }
    return Date(this.time + add)
}

fun String.dropEsChars(): String {
    val res = StringBuilder()
    this.forEach { char ->
        if (!ES_DROP_CHARS.contains(char)) res.append(char)
    }
    return res.toString()
}

fun customDateConvertor(year: Long?, month: Long?, day: Long?): LocalDate? {
    return if (year != null && month != null && day != null) {
        runCatching {
            LocalDate.of(year.toInt(), month.toInt(), day.toInt())
        }.getOrNull()

    } else {
        null
    }
}

class IntToBooleanDeserializer : JsonDeserializer<Boolean>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext?): Boolean {
        val intValue = parser.valueAsInt
        return intValue == 1
    }
}


class TypeDeserializer : JsonDeserializer<Type>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext?): Type {
        val typeValue = parser.text
        return when (typeValue.lowercase()) {
            "organization" -> Type.Organization
            "individual" -> Type.Individual
            "vessel" -> Type.Vessel
            "aircraft" -> Type.Aircraft
            "country" -> Type.Country
            else -> Type.Unknown
        }
    }
}

inline fun <T> Boolean.ifTrue(function: () -> T): T? {
    return if (this) function() else null
}

fun queryStringToMap(query: String): Map<String, String> {
    return query.split("&")
        .map { it.split("=", limit = 2) }
        .associate { it[0] to it.getOrElse(1) { "" } }
}

fun roundToInt(value: Double): Int {
    return Math.round(value).toInt()
}

fun localDateToString(date: LocalDate): String {
    return runCatching {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        date.format(formatter)
    }.getOrDefault("")
}