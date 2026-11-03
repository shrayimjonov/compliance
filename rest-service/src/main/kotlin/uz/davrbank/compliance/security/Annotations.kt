package uz.davrbank.compliance.security

import org.springframework.security.access.prepost.PreAuthorize

@Target(AnnotationTarget.FUNCTION)
@PreAuthorize("hasAuthority('DEV')")
annotation class IsDev()

@Target(AnnotationTarget.FUNCTION)
@PreAuthorize("hasAuthority('ADMIN')")
annotation class IsAdmin()

