package uz.davrbank.compliance.dto.request.filter

import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize(using = DictionarySearchFilterDeserializer::class)
class DictionarySearchFilter

class DictionarySearchFilterDeserializer : ValueDeserializer<DictionarySearchFilter>() {
    override fun deserialize(
        parser: JsonParser,
        context: DeserializationContext,
    ): DictionarySearchFilter {
        parser.skipChildren()
        return DictionarySearchFilter()
    }
}