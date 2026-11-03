package uz.davrbank.compliance.dbconsumer.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import uz.davrbank.compliance.dbconsumer.enums.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val ES_DROP_CHARS = setOf('\'', '-', '"', '.', ',', ' ', '(', ')')

fun String.dropEsChars(): String {
    val res = StringBuilder()
    this.forEach { char ->
        if (!ES_DROP_CHARS.contains(char)) res.append(char)
    }
    return res.toString()
}

fun localDateToString(date: LocalDate): String {
    return runCatching {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        date.format(formatter)
    }.getOrDefault("")
}

class IntToBooleanDeserializer : JsonDeserializer<Boolean>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext?): Boolean {
        return parser.valueAsInt == 1
    }
}

class TypeDeserializer : JsonDeserializer<Type>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext?): Type {
        return when (parser.text.lowercase()) {
            "organization" -> Type.Organization
            "individual"   -> Type.Individual
            "vessel"       -> Type.Vessel
            "aircraft"     -> Type.Aircraft
            "country"      -> Type.Country
            else           -> Type.Unknown
        }
    }
}
