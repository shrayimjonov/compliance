package uz.davrbank.compliance.dbconsumer.dto.kafka

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import uz.davrbank.compliance.dbconsumer.util.IntToBooleanDeserializer

data class EntitySOEsKafka(
    @JsonProperty("EntitySOE")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entitySOE: List<EntitySOEKafka>?
)

data class EntitySOEKafka(
    @JsonProperty("EntitySOEGUID")
    val entitySOEGUID: String,
    @JsonProperty("IsActive")
    @JsonDeserialize(using = IntToBooleanDeserializer::class)
    val isActive: Boolean,
    @JsonProperty("LastUpdated")
    val lastUpdated: String,
    @JsonProperty("EntitySOEDomains")
    val entitySOEDomains: EntitySOEDomainsKafka?,
    @JsonProperty("EntitySOESubcategories")
    val entitySOESubcategories: EntitySOESubcategoriesKafka?
)

data class EntitySOEDomainsKafka(
    @JsonProperty("EntitySOEDomain")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val entitySOEDomain: List<EntitySOEDomainKafka>?
)

data class EntitySOEDomainKafka(
    @JsonProperty("EntitySOEDomainGUID")
    val entitySOEDomainGUID: String,
    @JsonProperty("SOEDomainDesc")
    val soeDomainDesc: String,
    @JsonProperty("LastUpdated")
    val lastUpdated: String
)

data class EntitySOESubcategoriesKafka(
    @JsonProperty("Subcategory")
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val subcategory: List<EntitySOESubcategoryKafka>?
)

data class EntitySOESubcategoryKafka(
    @JsonProperty("EntitySOESubCategoryGUID")
    val entitySOESubcategoryGUID: String?,
    @JsonProperty("SubCategoryLabel")
    val subCategoryLabel: String?,
    @JsonProperty("LastUpdated")
    val lastUpdated: String?,
)
