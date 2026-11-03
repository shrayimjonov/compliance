package uz.davrbank.compliance.service

import org.springframework.data.domain.Page
import uz.davrbank.compliance.dto.request.filter.BaseFilter
import uz.davrbank.compliance.dto.request.filter.LocalComplianceListFilter
import uz.davrbank.compliance.dto.request.local.ComplianceAdminRequest
import uz.davrbank.compliance.dto.response.MessageResponse
import uz.davrbank.compliance.dto.response.local.ComplianceAdminResponse

interface LocalComplianceService {
    fun create(request: ComplianceAdminRequest): ComplianceAdminResponse
    fun getList(filter: BaseFilter<LocalComplianceListFilter>): Page<ComplianceAdminResponse>
    fun get(id: String): ComplianceAdminResponse
    fun update(id: String, request: ComplianceAdminRequest): ComplianceAdminResponse
    fun delete(id: String): MessageResponse
}
