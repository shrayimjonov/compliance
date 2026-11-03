package uz.davrbank.compliance.local

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uz.davrbank.compliance.SparkUserRequest
import uz.davrbank.compliance.SparkUserResponse
import uz.davrbank.compliance.SparkUserService

@RestController
@RequestMapping("api/v1/compliance")
class LocalComplianceController(
    private val localComplianceService: LocalComplianceService
) {
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("local")
    fun getAll(pageable: Pageable) = localComplianceService.getAll(pageable)

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("local/{id}")
    fun getOne(@PathVariable id: String) = localComplianceService.getOne(id)

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("local")
    fun addData(@RequestBody request: ComplianceAdminRequest) = localComplianceService.addData(request)

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("local/{id}")
    fun updateData(@PathVariable id: String, @RequestBody request: ComplianceAdminRequest) =
        localComplianceService.update(id, request)

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("local/{id}")
    fun deleteData(@PathVariable id: Long) = localComplianceService.delete(id)


}


@RestController
@RequestMapping("api/v1/spark-user")
class SparkUserController(
    private val sparkUserService: SparkUserService
) {

    @PreAuthorize("hasAuthority('DEV')")
    @PostMapping
    fun createUser(@RequestBody request: SparkUserRequest) {
         sparkUserService.add(request)
    }

    @PreAuthorize("hasAuthority('DEV')")
    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody request: SparkUserRequest): SparkUserResponse {
        return sparkUserService.update(id, request)
    }

    @PreAuthorize("hasAuthority('DEV')")
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long) {
        sparkUserService.delete(id)
    }

    @PreAuthorize("hasAuthority('DEV')")
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): SparkUserResponse {
        return sparkUserService.getOne(id)
    }

    @PreAuthorize("hasAuthority('DEV')")
    @GetMapping
    fun getAllUsers(pageable: Pageable): Page<SparkUserResponse> {
        return sparkUserService.getAll(pageable)
    }
}