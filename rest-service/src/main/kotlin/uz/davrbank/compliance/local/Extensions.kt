package uz.davrbank.compliance.local



fun ComplianceSearchRequest.hashForCache() = "$target#${lastname?.trim()}#${firstname?.trim()}#${name?.trim()}"


