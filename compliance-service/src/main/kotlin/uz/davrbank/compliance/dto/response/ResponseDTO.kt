package uz.davrbank.compliance.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import uz.davrbank.compliance.exception.ErrorDTO

@JsonIgnoreProperties(ignoreUnknown = true)
data class ResponseDTO<T>(
    @JsonProperty("success")
    val success: Boolean,
    @JsonProperty("data")
    val data: T? = null,
    @JsonProperty("requestId")
    val requestId: String? = null,
    @JsonProperty("error")
    val error: ErrorDTO? = null,
) {
    companion object {
        fun <T> ok(body: T): ResponseDTO<T> = ResponseDTO(success = true, data = body)

        fun <T> ok(body: T, requestId: String): ResponseDTO<T> =
            ResponseDTO(success = true, data = body, requestId = requestId)

        fun error(error: ErrorDTO): ResponseDTO<Nothing> = ResponseDTO(success = false, error = error)
    }
}
