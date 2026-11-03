package uz.davrbank.compliance.controller.dictionary

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uz.davrbank.compliance.constants.ApiConstants
import uz.davrbank.compliance.dto.request.filter.BaseFilter
import uz.davrbank.compliance.dto.request.filter.DictionarySearchFilter
import uz.davrbank.compliance.dto.request.filter.OrgType
import uz.davrbank.compliance.dto.response.BranchDictionaryItem
import uz.davrbank.compliance.dto.response.ContractTypeDictionaryItem
import uz.davrbank.compliance.dto.response.CountryDictionaryItem
import uz.davrbank.compliance.dto.response.CurrencyDictionaryItem
import uz.davrbank.compliance.dto.response.EnumDictionaryResponse
import uz.davrbank.compliance.dto.response.NationalityDictionaryItem
import uz.davrbank.compliance.dto.response.ResponseDTO
import uz.davrbank.compliance.dto.response.ResponsePageDTO
import uz.davrbank.compliance.enums.AbsCheckOperationType
import uz.davrbank.compliance.enums.AbsComplianceStatus
import uz.davrbank.compliance.enums.AbsHandlerStatus
import uz.davrbank.compliance.enums.AbsRequestType
import uz.davrbank.compliance.enums.ComplianceType
import uz.davrbank.compliance.enums.DataType
import uz.davrbank.compliance.enums.EntityType
import uz.davrbank.compliance.enums.Type
import uz.davrbank.compliance.util.DictionaryMemoryStore

@Validated
@RestController
@RequestMapping(ApiConstants.DICTIONARY_ROOT_API)
class DictionaryController(
    private val dictionaryMemoryStore: DictionaryMemoryStore,
) {

    @PostMapping(ApiConstants.COUNTRIES)
    fun getCountries(
        @Valid @RequestBody filter: BaseFilter<DictionarySearchFilter>,
    ): ResponseEntity<ResponseDTO<ResponsePageDTO<CountryDictionaryItem>>> = ResponseEntity.ok(
        ResponseDTO.ok(dictionaryMemoryStore.getCountriesPage(filter))
    )

    @PostMapping(ApiConstants.NATIONALITIES)
    fun getNationalities(
        @Valid @RequestBody filter: BaseFilter<DictionarySearchFilter>,
    ): ResponseEntity<ResponseDTO<ResponsePageDTO<NationalityDictionaryItem>>> = ResponseEntity.ok(
        ResponseDTO.ok(dictionaryMemoryStore.getNationalitiesPage(filter))
    )

    @PostMapping(ApiConstants.BRANCHES)
    fun getBranches(
        @Valid @RequestBody filter: BaseFilter<DictionarySearchFilter>,
    ): ResponseEntity<ResponseDTO<ResponsePageDTO<BranchDictionaryItem>>> = ResponseEntity.ok(
        ResponseDTO.ok(dictionaryMemoryStore.getBranchesPage(filter))
    )

    @PostMapping(ApiConstants.CONTRACT_TYPES)
    fun getContractTypes(
        @Valid @RequestBody filter: BaseFilter<DictionarySearchFilter>,
    ): ResponseEntity<ResponseDTO<ResponsePageDTO<ContractTypeDictionaryItem>>> = ResponseEntity.ok(
        ResponseDTO.ok(dictionaryMemoryStore.getContractTypesPage(filter))
    )

    @PostMapping(ApiConstants.CURRENCIES)
    fun getCurrencies(
        @Valid @RequestBody filter: BaseFilter<DictionarySearchFilter>,
    ): ResponseEntity<ResponseDTO<ResponsePageDTO<CurrencyDictionaryItem>>> = ResponseEntity.ok(
        ResponseDTO.ok(dictionaryMemoryStore.getCurrenciesPage(filter))
    )

    @GetMapping(ApiConstants.ENUMS)
    fun getEnums(): ResponseEntity<ResponseDTO<List<EnumDictionaryResponse>>> = ResponseEntity.ok(
        ResponseDTO.ok(ENUM_DICTIONARIES)
    )

    private companion object {
        private val ENUM_DICTIONARIES = listOf(
            enumDictionary<AbsHandlerStatus>(),
            enumDictionary<AbsComplianceStatus>(),
            enumDictionary<AbsCheckOperationType>(),
            enumDictionary<AbsRequestType>(),
            enumDictionary<Type>(),
            enumDictionary<ComplianceType>(),
            enumDictionary<DataType>(),
            entityTypeDictionary(),
            enumDictionary<OrgType>(),
        )

        private fun entityTypeDictionary(): EnumDictionaryResponse =
            EnumDictionaryResponse(
                name = EntityType::class.simpleName.orEmpty(),
                values = EntityType.entries.map { it.value },
            )

        private inline fun <reified T : Enum<T>> enumDictionary(): EnumDictionaryResponse =
            EnumDictionaryResponse(
                name = T::class.simpleName.orEmpty(),
                values = enumValues<T>().map { it.name },
            )
    }
}
