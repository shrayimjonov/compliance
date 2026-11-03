package uz.davrbank.compliance.dto.request.filter

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort


open class BaseFilter<T>(
    open var requestId: String? = null,
    open var keyword: String? = null,
    @field:Valid
    open var params: T? = null,
    @field:Valid
    @field:NotNull
    open var pagination: Pagination? = null,
) {
    data class Pagination(
        @field:Min(0)
        val page: Int = DEFAULT_PAGE,
        val size: Int = DEFAULT_SIZE,
        val orderBy: String? = DEFAULT_ORDER_BY,
        val sortOrder: String? = DEFAULT_SORT_ORDER,
    ) {
        fun toPageable(maxPageSize: Int): Pageable {
            val boundedPage = page.coerceAtLeast(0)
            val boundedSize = size.coerceIn(1, maxPageSize.coerceAtLeast(1))
            return PageRequest.of(boundedPage, boundedSize, orderedSortBy())
        }

        private fun orderedSortBy(): Sort {
            val property = orderBy?.takeIf { it.isNotBlank() } ?: DEFAULT_ORDER_BY
            return if (sortOrder?.equals(SORT_ASC, ignoreCase = true) == true) {
                Sort.by(property).ascending()
            } else {
                Sort.by(property).descending()
            }
        }
    }

    companion object {
        private const val DEFAULT_PAGE = 0
        private const val DEFAULT_SIZE = 20
        private const val DEFAULT_ORDER_BY = "id"
        private const val DEFAULT_SORT_ORDER = "desc"
        private const val SORT_ASC = "asc"
    }
}
