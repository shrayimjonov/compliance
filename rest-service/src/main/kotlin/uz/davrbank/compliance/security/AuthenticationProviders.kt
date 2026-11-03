package uz.davrbank.compliance.security

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import uz.davrbank.compliance.UserAuthorizationRepository

class CustomAuthenticationProvider(
    private val userAuthorizationRepository: UserAuthorizationRepository
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        authentication as UsernamePasswordAuthenticationToken
        val accessToken = authentication.credentials?.toString() ?: return authentication
        val authorization =
            userAuthorizationRepository.findByAccessTokenAndDeletedFalse(accessToken) ?: return authentication
        if (!authorization.user.active || authorization.isAccessTokenExpired() ) return authentication
        val authenticated = UsernamePasswordAuthenticationToken.authenticated(
            authorization.user.username,
            null,
            listOf(SimpleGrantedAuthority(authorization.user.role.name))
        )
        authenticated.details = UserDetails.toUserDetails(authorization.user)
        return authenticated
    }

    override fun supports(authentication: Class<*>?) =
        authentication?.isAssignableFrom(UsernamePasswordAuthenticationToken::class.java) ?: false
}