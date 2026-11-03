package uz.davrbank.compliance.risk

import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uz.davrbank.compliance.local.ComplianceSearchRequest


@RestController
@RequestMapping("api/v1/risk")
class RiskController(
    private val riskService: RiskService
) {
    @GetMapping
    fun getIndividualRiskInfo(@RequestParam id: String) = riskService.getIndividualRiskProfileV3(id)

    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEV')")
    @PostMapping("check")
    fun check(@RequestBody request: ComplianceSearchRequest) = riskService.checkComplianceMaxHit(request)

    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEV')")
    @GetMapping("check-data")
    fun checkData(request: ComplianceSearchRequest, pageable: Pageable) = riskService.checkData(request, pageable)


    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEV')")
    @GetMapping("search")
    fun search(request: ComplianceSearchRequest, pageable: Pageable) = riskService.search(request, pageable)

}