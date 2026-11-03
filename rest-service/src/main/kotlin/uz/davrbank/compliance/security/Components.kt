package uz.davrbank.compliance.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import uz.davrbank.compliance.MyUser
import uz.davrbank.compliance.UserRepository
import uz.davrbank.compliance.UserRole

@Component
class Initializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    init {
        initDevUser()
    }

    private fun initDevUser() {
        if (userRepository.existsByUsernameAndDeletedFalse("dev")) return
        val devUser = MyUser(
            "dev",
//            "$2a$12\$REDACTEDREDACTEDREDACTEDREDACTEDREDACTEDREDACTEDREDAC",
            passwordEncoder.encode("123456"),
            "Botir",
            "Adoshboyev",
            null,
            UserRole.DEV
        )
        userRepository.save(devUser)
    }
}

fun username(): String =
    (SecurityContextHolder.getContext().authentication as UsernamePasswordAuthenticationToken).principal?.toString()
        ?: "NotFoundUsername"
