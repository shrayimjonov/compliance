package uz.davrbank.compliance.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import uz.davrbank.compliance.ApiLogRepository
import uz.davrbank.compliance.CustomAuthenticationEntryPoint
import uz.davrbank.compliance.UserAuthorizationRepository


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val apiLogRepository: ApiLogRepository
) {


    @Bean
    fun filterChain(
        http: HttpSecurity,
        authenticationManager: AuthenticationManager
    ): SecurityFilterChain {
        http
            .sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { csrf -> csrf.disable() }
            .authorizeHttpRequests { request ->
                request.requestMatchers(HttpMethod.POST, "/api/v1/auth/token").permitAll()
                request.anyRequest().authenticated()
            }
            .addFilterBefore(
                BearerTokenSecurityFilter(authenticationManager, apiLogRepository),
                BasicAuthenticationFilter::class.java
            )
            .exceptionHandling { handling ->
                handling.authenticationEntryPoint(CustomAuthenticationEntryPoint())
            }
            .httpBasic { basic -> basic.disable() }
            .formLogin { formLogin -> formLogin.disable() }

        return http.build()
    }

    @Bean
    fun authenticationManager(
        userAuthorizationRepository: UserAuthorizationRepository
    ): AuthenticationManager {
        val authenticationProvider = CustomAuthenticationProvider(userAuthorizationRepository)
        val providerManager = ProviderManager(authenticationProvider)
        return providerManager
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()
}