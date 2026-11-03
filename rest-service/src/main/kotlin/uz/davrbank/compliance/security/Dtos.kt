package uz.davrbank.compliance.security

import com.fasterxml.jackson.annotation.JsonProperty
import uz.davrbank.compliance.MyUser
import uz.davrbank.compliance.UserAuthorization

data class TokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("refresh_token") val refreshToken: String,
    @JsonProperty("expires_in") val expiresIn: Long
) {
    companion object {
        fun toTokenResponse(userAuthorization: UserAuthorization) =
            userAuthorization.run { TokenResponse(accessToken, refreshToken, accessTokenExpiresIn()) }
    }
}

data class UserDetails(
    val fullName: String
) {
    companion object {
        fun toUserDetails(user: MyUser) = user.run { UserDetails(fullName()) }
    }
}