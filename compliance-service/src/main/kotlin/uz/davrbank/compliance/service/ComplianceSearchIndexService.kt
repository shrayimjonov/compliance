package uz.davrbank.compliance.service

import uz.davrbank.compliance.domain.Compliance

interface ComplianceSearchIndexService {
    fun save(compliance: Compliance)
    fun delete(complianceId: String)
}
