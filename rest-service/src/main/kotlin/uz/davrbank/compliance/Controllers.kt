package uz.davrbank.compliance


import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uz.davrbank.compliance.services.AbsDataCheckerService


@RestController
@RequestMapping("api/v1/user")
class UserController(
    private val userService: UserService
) {

    @GetMapping("me")
    fun me() = userService.me()


    @GetMapping("{id}")
    fun findById(@PathVariable id: Long) = userService.findById(id)


    @PreAuthorize("hasAuthority('DEV')")
    @GetMapping
    fun getAll(pageable: Pageable) = userService.getAll(pageable)


    @PreAuthorize("hasAuthority('DEV')")
    @PostMapping
    fun addUser(@RequestBody request: UserAdminRequest) = userService.addUser(request)


    @PreAuthorize("hasAuthority('DEV')")
    @PutMapping("{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody request: UserAdminRequest) = userService.updateUser(id, request)


    @PreAuthorize("hasAuthority('DEV')")
    @DeleteMapping("{id}")
    fun deleteUser(@PathVariable id: Long) = userService.deleteUser(id)
}


@RestController
@RequestMapping("api/v1/abs-checker")
class AbsCheckerController(
    private val absDataCheckerService: AbsDataCheckerService,
) {

    @PreAuthorize("hasAnyAuthority('DEV','ADMIN')")
    @GetMapping
    fun getAllFinished(
        pageable: Pageable,
        @RequestParam status: ComplianceStatus?,
        @RequestParam operationType: AbsCheckOperationType?,
        @RequestParam startDate: Long?,
        @RequestParam endDate: Long?,
        @RequestParam isResident: Boolean?
    ) = absDataCheckerService.getAllData(pageable, status, operationType, startDate, endDate, isResident)

    @GetMapping("{id}")
    fun getData(@PathVariable id: String, pageable: Pageable) = absDataCheckerService.getOne(id, pageable)

    @PostMapping("check-account")
    fun checkAccount(@RequestBody request: AbsDataCheckerRequest) = absDataCheckerService.saveData(request)


    @GetMapping("check-status")
    fun checkOpenAccountRequestStatus(@RequestParam id: String) =
        absDataCheckerService.checkStatus(id)


    // admin tarafidan kelgan so'rov ga approve berish yoki reject qilish
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("change-status/{id}")
    fun changeStatus(@PathVariable id: String, @RequestBody request: AbsDataCheckerChangeStatusRequest) =
        absDataCheckerService.changeStatus(id, request)

    @PreAuthorize("hasAnyAuthority('DEV','ADMIN')")
    @GetMapping("logs/yduashdbt")
    fun getLogs(pageable: Pageable) = absDataCheckerService.getLogs(pageable)

}


@RestController
@RequestMapping("api/v1/file")
class FileController(
    private val fileService: FileService
) {
    @PreAuthorize("hasAnyAuthority('DEV')")
    @GetMapping
    fun getAll(pageable: Pageable) = fileService.getAll(pageable)

    @PreAuthorize("hasAnyAuthority('DEV')")
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long) = fileService.getOne(id)

    @PreAuthorize("hasAnyAuthority('DEV')")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = fileService.delete(id)

    @PreAuthorize("hasAnyAuthority('DEV')")
    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: FileAssetUpdateRequest) = fileService.update(id, request)


    @PreAuthorize("hasAnyAuthority('DEV')")
    @PostMapping("upload-lexis")
    fun uploadData(@RequestParam fileId: Long) = fileService.uploadData(fileId)
}