package uz.davrbank.compliance.dto.request.filter

import uz.davrbank.compliance.enums.Type

enum class OrgType {
    J,
    I,
    B,
    S;

    fun toSearchType(): Type = when (this) {
        J -> Type.Organization
        I -> Type.Individual
        B -> Type.BIG
        S -> Type.SWIFT
    }
}
