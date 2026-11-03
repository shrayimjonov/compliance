package uz.davrbank.compliance.integration.davrhub

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

data class DavrHubAccessToken(
    val accessToken: String,
    val tokenType: String = DavrHubConstants.DEFAULT_TOKEN_TYPE,
    val expiresInSeconds: Long,
) {
    fun authorizationHeaderValue(): String =
        "${tokenType.ifBlank { DavrHubConstants.DEFAULT_TOKEN_TYPE }} $accessToken"
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class DavrHubTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String? = null,
    @JsonProperty("token_type")
    val tokenType: String? = null,
    @JsonProperty("expires_in")
    val expiresIn: Long = 0,
    @JsonProperty("scope")
    val scope: String? = null,
) {
    fun toAccessToken(): DavrHubAccessToken {
        val token = accessToken
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: throw DavrHubCommunicationException("DavrHub token response did not contain access_token")

        return DavrHubAccessToken(
            accessToken = token,
            tokenType = tokenType?.trim()?.takeIf(String::isNotBlank) ?: DavrHubConstants.DEFAULT_TOKEN_TYPE,
            expiresInSeconds = expiresIn.coerceAtLeast(1),
        )
    }
}

data class LexisRequest(
    val requestId: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LexisResponse(
    val lexId: String,
)
