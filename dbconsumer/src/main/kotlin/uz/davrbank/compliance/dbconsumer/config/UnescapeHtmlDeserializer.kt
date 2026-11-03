package uz.davrbank.compliance.dbconsumer.config
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.apache.commons.text.StringEscapeUtils

class UnescapeHtmlDeserializer : StdDeserializer<String?>(String::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): String? {
        return p.valueAsString?.let { StringEscapeUtils.unescapeXml(it) }
    }
}