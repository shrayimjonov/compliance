package uz.davrbank.compliance.util

import org.springframework.stereotype.Component
import uz.davrbank.compliance.dto.request.filter.BaseFilter
import uz.davrbank.compliance.dto.request.filter.DictionarySearchFilter
import uz.davrbank.compliance.dto.response.BranchDictionaryItem
import uz.davrbank.compliance.dto.response.ContractTypeDictionaryItem
import uz.davrbank.compliance.dto.response.CountryDictionaryItem
import uz.davrbank.compliance.dto.response.CurrencyDictionaryItem
import uz.davrbank.compliance.dto.response.NationalityDictionaryItem
import uz.davrbank.compliance.dto.response.ResponsePageDTO

@Component
class DictionaryMemoryStore {
    private val countries: List<CountryDictionaryItem> = DictionaryData.countries
    private val nationalities: List<NationalityDictionaryItem> = DictionaryData.nationalities
    private val branches: List<BranchDictionaryItem> = DictionaryData.branches
    private val contractTypes: List<ContractTypeDictionaryItem> = DictionaryData.contractTypes
    private val currencies: List<CurrencyDictionaryItem> = DictionaryData.currencies

    private val countryNameByCode: Map<String, String> = countries.associate { it.countryCode to it.nameRu }
    private val branchNameByCode: Map<String, String> = branches.associate { it.branchCode to it.branchName }
    private val contractTypeNameByCode: Map<String, String> = contractTypes.associate { it.code to it.name }
    private val currencyNameByCode: Map<String, String> = currencies.associate { it.currencyCode to it.name }

    fun getCountries(filter: BaseFilter<DictionarySearchFilter> = dictionaryFilter()): List<CountryDictionaryItem> =
        countries.filterBy(filter.keyword) {
            listOf(countryCode, name, nameUz, nameRu)
        }

    fun getNationalities(filter: BaseFilter<DictionarySearchFilter> = dictionaryFilter()): List<NationalityDictionaryItem> =
        nationalities.filterBy(filter.keyword) {
            listOf(countryCode, name, nameUz, nameRu)
        }

    fun getBranches(filter: BaseFilter<DictionarySearchFilter> = dictionaryFilter()): List<BranchDictionaryItem> =
        branches.filterBy(filter.keyword) {
            listOf(branchCode, branchName)
        }

    fun getContractTypes(filter: BaseFilter<DictionarySearchFilter> = dictionaryFilter()): List<ContractTypeDictionaryItem> =
        contractTypes.filterBy(filter.keyword) {
            listOf(code, name)
        }

    fun getCurrencies(filter: BaseFilter<DictionarySearchFilter> = dictionaryFilter()): List<CurrencyDictionaryItem> =
        currencies.filterBy(filter.keyword) {
            listOf(currencyCode, name)
        }

    fun getCountriesPage(
        filter: BaseFilter<DictionarySearchFilter> = dictionaryFilter(),
    ): ResponsePageDTO<CountryDictionaryItem> = getCountries(filter).toResponsePage(filter.pagination)

    fun getNationalitiesPage(
        filter: BaseFilter<DictionarySearchFilter> = dictionaryFilter(),
    ): ResponsePageDTO<NationalityDictionaryItem> = getNationalities(filter).toResponsePage(filter.pagination)

    fun getBranchesPage(
        filter: BaseFilter<DictionarySearchFilter> = dictionaryFilter(),
    ): ResponsePageDTO<BranchDictionaryItem> = getBranches(filter).toResponsePage(filter.pagination)

    fun getContractTypesPage(
        filter: BaseFilter<DictionarySearchFilter> = dictionaryFilter(),
    ): ResponsePageDTO<ContractTypeDictionaryItem> = getContractTypes(filter).toResponsePage(filter.pagination)

    fun getCurrenciesPage(
        filter: BaseFilter<DictionarySearchFilter> = dictionaryFilter(),
    ): ResponsePageDTO<CurrencyDictionaryItem> = getCurrencies(filter).toResponsePage(filter.pagination)

    fun countryName(code: String?): String = resolve(code, countryNameByCode)

    fun branchName(code: String?): String = resolve(code, branchNameByCode)

    fun contractTypeName(code: String?): String = resolve(code, contractTypeNameByCode)

    fun currencyName(code: String?): String = resolve(code, currencyNameByCode)

    private fun resolve(code: String?, lookup: Map<String, String>): String {
        val key = code?.trim().orEmpty()
        if (key.isEmpty()) return ""
        return lookup[key] ?: key
    }

    private fun dictionaryFilter(): BaseFilter<DictionarySearchFilter> = BaseFilter(
        params = DictionarySearchFilter(),
        pagination = BaseFilter.Pagination(),
    )

    private fun <T> List<T>.filterBy(
        rawKeyword: String?,
        searchableValues: T.() -> List<String>,
    ): List<T> {
        val keyword = rawKeyword?.trim()?.takeIf { it.isNotBlank() }?.lowercase() ?: return this

        return filter { item ->
            item.searchableValues().any { value ->
                value.lowercase().contains(keyword)
            }
        }
    }

    private fun <T : Any> List<T>.toResponsePage(
        pagination: BaseFilter.Pagination?,
    ): ResponsePageDTO<T> {
        val resolvedPagination = pagination ?: BaseFilter.Pagination()
        val page = resolvedPagination.page.coerceAtLeast(0)
        val pageSize = resolvedPagination.size.coerceIn(1, MAX_PAGE_SIZE)
        val total = this.size
        val offset = page.toLong() * pageSize
        val pageItems = if (offset >= total.toLong()) {
            emptyList()
        } else {
            drop(offset.toInt()).take(pageSize)
        }

        return ResponsePageDTO(
            list = pageItems,
            total = total.toLong(),
            page = page,
            size = pageSize,
        )
    }

    companion object {
        private const val MAX_PAGE_SIZE = 100
    }
}
