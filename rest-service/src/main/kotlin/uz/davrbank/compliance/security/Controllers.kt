package uz.davrbank.compliance.security

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController



@RestController
@RequestMapping("api/v1/auth")
class AuthController(
    private val tokenService: TokenService
) {

    @PostMapping("token")
    fun token(
        @RequestParam(required = false) username: String?,
        @RequestParam(required = false) password: String?,
        @RequestParam(value = "refresh_token", required = false) refreshToken: String?,
        @RequestParam(value = "grant_type") grantType: String
    ) = tokenService.token(username, password, refreshToken, grantType)

}