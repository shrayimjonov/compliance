package uz.davrbank.compliance.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.domain.Page

@JsonIgnoreProperties(ignoreUnknown = true)
data class ResponsePageDTO<T : Any>(
    @JsonProperty("list")
    val list: List<T>,
    @JsonProperty("total")
    val total: Long,
    @JsonProperty("page")
    val page: Int,
    @JsonProperty("size")
    val size: Int,
)

fun <T : Any> Page<T>.toResponsePageDTO(): ResponsePageDTO<T> = ResponsePageDTO(
    list = content,
    total = totalElements,
    page = number,
    size = size,
)
