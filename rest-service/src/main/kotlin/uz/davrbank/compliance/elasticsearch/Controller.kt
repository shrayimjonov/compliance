package uz.davrbank.compliance.elasticsearch

import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/es")
class EsController(
    private val esCheckService: ESCheckService,
    private val esService: ESService
) {

    @PreAuthorize("hasAuthority('DEV')")
    @GetMapping
    fun search(@RequestParam firstname: String, @RequestParam lastname: String, pageable: Pageable) =
        esCheckService.findIndividuals(firstname, lastname, pageable)

    @PreAuthorize("hasAuthority('DEV')")
    @DeleteMapping
    fun delete() = esService.deleteAllData()
}