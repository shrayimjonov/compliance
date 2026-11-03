package uz.davrbank.compliance.security

import org.apache.commons.lang3.RandomStringUtils
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import uz.davrbank.compliance.*
import java.util.*
import java.util.concurrent.TimeUnit


interface TokenService {
    fun token(username: String?, password: String?, refreshToken: String?, grantType: String): TokenResponse
}

@Service
class TokenServiceImpl(
    private val userRepository: UserRepository,
    private val userAuthorizationRepository: UserAuthorizationRepository,
    private val passwordEncoder: PasswordEncoder
) : TokenService {

    override fun token(username: String?, password: String?, refreshToken: String?, grantType: String): TokenResponse {
        return when (grantType) {
            PASSWORD_GRANT_TYPE -> {
                if (username == null || password == null) throw GeneralApiException("Username or password is incorrect")
                accessToken(username, password)
            }

            REFRESH_TOKEN_GRANT_TYPE -> {
                if (refreshToken == null) throw GeneralApiException("Invalid refresh token")
                refreshToken(refreshToken)
            }

            else -> throw UnsupportedGrantTypeException(grantType)
        }
    }

    private fun accessToken(username: String, password: String): TokenResponse {
        val user = userRepository.findByUsernameAndDeletedFalse(username)
            ?: throw GeneralApiException("Username or password is incorrect")
        if (!passwordEncoder.matches(
                password,
                user.password
            )
        ) throw GeneralApiException("Username or password is incorrect")

        // get existing token OR if token is expired, generate new token
        val existingAuthorization = userAuthorizationRepository.findByUserIdAndDeletedFalse(user.id!!)
            ?: return generateToken(user)
        if (existingAuthorization.isAccessTokenExpired()) {
            userAuthorizationRepository.delete(existingAuthorization)
            return generateToken(user)
        }
        return TokenResponse.toTokenResponse(existingAuthorization)
    }

    private fun refreshToken(refreshToken: String): TokenResponse {
        val userAuthorization = userAuthorizationRepository.findByRefreshTokenAndDeletedFalse(refreshToken)
            ?: throw GeneralApiException("Invalid refresh token")
        if (userAuthorization.user.deleted) {
            userAuthorizationRepository.delete(userAuthorization)
            throw GeneralApiException("Invalid refresh token")
        }
        if (userAuthorization.isRefreshTokenExpired() || userAuthorization.isAccessTokenExpired()) {
            userAuthorizationRepository.delete(userAuthorization)
            throw GeneralApiException("Invalid refresh token")
        }
        if (!userAuthorization.isAccessTokenExpired()) return TokenResponse.toTokenResponse(userAuthorization)
        return generateToken(userAuthorization.user)
    }

    private fun generateToken(user: MyUser): TokenResponse {
        val accessToken = stringToken(ACCESS_TOKEN_LENGTH)
        val refreshToken = stringToken(REFRESH_TOKEN_LENGTH)
        val accessTokenExpiresAt = Date().plus(ACCESS_TOKEN_TTL, TimeUnit.MINUTES)
        val refreshTokenExpiresAt = Date().plus(REFRESH_TOKEN_TTL, TimeUnit.MINUTES)
        val userAuthorization =
            UserAuthorization(accessToken, refreshToken, accessTokenExpiresAt, refreshTokenExpiresAt, user)
        userAuthorizationRepository.save(userAuthorization)
        return TokenResponse.toTokenResponse(userAuthorization)
    }

    private fun stringToken(length: Int): String {
        return RandomStringUtils.randomAlphanumeric(length).lowercase()
    }

    companion object {
        const val ACCESS_TOKEN_TTL = 360L // min
        const val REFRESH_TOKEN_TTL = 1080L // min
        const val ACCESS_TOKEN_LENGTH = 50
        const val REFRESH_TOKEN_LENGTH = 30
        const val PASSWORD_GRANT_TYPE = "password"
        const val REFRESH_TOKEN_GRANT_TYPE = "refresh_token"
    }
}
