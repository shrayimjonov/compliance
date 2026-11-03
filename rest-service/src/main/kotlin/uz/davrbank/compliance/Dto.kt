package uz.davrbank.compliance

import uz.davrbank.compliance.backup.Compliance
import uz.davrbank.compliance.backup.Type
import uz.davrbank.compliance.elasticsearch.ESHits
import java.time.LocalDate
import java.util.Date
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import uz.davrbank.compliance.elasticsearch.NewComplianceData
import java.math.BigDecimal

data class ContextObject(
    var log: ApiLog,
)

interface CustomContext {
    fun getObject(): ContextObject?

    fun setObject(context: ContextObject)
}

data class BaseMessage(
    val code: Long? = null,
    val message: String? = null
)

data class UserDto(
    val username: String,
    val fullName: String,
    val role: String,
    val id: Long?
) {

    companion object {
        fun toDto(user: MyUser) = user.run { UserDto(username, fullName(), role.name, id) }
    }
}

data class UserAdminRequest(
    val username: String,
    val password: String?,
    var firstname: String,
    var lastname: String,
    var patronymic: String?,
    val role: UserRole,
)


data class UserResponse(
    val username: String,
    var firstname: String,
    var lastname: String,
    var patronymic: String?,
    val role: UserRole,
    val id: Long?
) {

    companion object {
        fun toDto(user: MyUser) = user.run { UserResponse(username, firstname, lastname, patronymic, role, id) }
    }
}

data class KafkaListenerEntityHandlerDto(
    val complianceEntityDoc: NewComplianceData,
    val compliance: Compliance
)


data class SparkUserRequest(
    val username: String,
    val password: String?,
    val active: Boolean
)


data class SparkUserResponse(
    val id: Long,
    val username: String,
    val active: Boolean
)


data class AbsDataCheckerRequest(
    var isResident: Boolean,
    var firstname: String?,
    var lastname: String?,
    var patronymic: String?,
    var gender: String?,
    var birthday: LocalDate?,
    var nationality: String?,
    var type: Type,
    var clientInn: String?,
    var orgName: String?,  // organization name
    var country: String?, // organization country
    var operationType: AbsCheckOperationType,
    var swiftCode: String?,
    var bankBik: String?,
    var address: String?,
    var director: String?,
    var directorInn: String?,
    var founder: String?,
    var founderInn: String?,
    var partnerName: String?,
    var partnerInn: String?,
    var orgRegistrationDate: Date?,
    var participationShare: BigDecimal?,
    var branch: String?,
    var actionType: String?,
    var requestedDate: Date?,
    var comment: String?
)


data class AbsDataCheckerAdminResponse(
    var id: String,
    var request: AbsDataCheckerRequest,
    var response: ESHits? = null,
    var complianceStatus: ComplianceStatus,
    var operationType: AbsCheckOperationType,
    var status: AbsHandlerStatus
) {
    companion object {
        fun toDto(absDataChecker: AbsDataChecker): AbsDataCheckerAdminResponse {
            return AbsDataCheckerAdminResponse(
                absDataChecker.id!!,
                absDataChecker.request,
                absDataChecker.response,
                absDataChecker.complianceStatus,
                absDataChecker.operationType,
                absDataChecker.status
            )
        }
    }
}



data class AbsDataCheckerAdminGetOneResponse (
    var id: String,
    var request: AbsDataCheckerRequest,
    var response: ESHits? = null,
){
    companion object {
        fun toDto(absDataChecker: AbsDataChecker): AbsDataCheckerAdminGetOneResponse {
            return AbsDataCheckerAdminGetOneResponse(
                absDataChecker.id!!,
                absDataChecker.request,
                absDataChecker.response
            )
        }
    }
}

data class AbsDataCheckerResponse(
    val id: String
)


data class CheckResultResponseWrapper(
    var status: Boolean,
    var data: AbsDataCheckerResultResponse? = null
)

data class AbsDataCheckerResultResponse(
    var id: String,
    var status: ComplianceStatus,
    var comment: String?
//    var request: AbsDataCheckerRequest
)


data class AbsDataCheckerChangeStatusRequest(
    var status: ComplianceStatus,
    var comment: String? = null,
)

data class FileAssetAllResponse (
    var id: Long,
    var fileName: String,
    var fileSize: Long,
    var type: FileType,
    var date: LocalDate? = null,
    @Enumerated(EnumType.STRING) var status: Status
){
    companion object {
        fun toDto(fileAsset: FileAsset) = fileAsset.run {
            FileAssetAllResponse(
                id!!,
                fileName,
                fileSize,
                type,
                date,
                status,

            )
        }
    }
}


data class FileAssetResponse (
    var id: Long,
    var fileName: String,
    var fileSize: Long,
    var type: FileType,
    @Enumerated(EnumType.STRING) var status: Status,
    var hashId: String,
    var uploadFolder: String,
    var uploadFileName: String,
    var date: LocalDate? = null,
    var isUploaded: Boolean = false,
){
    companion object {
        fun toDto(fileAsset: FileAsset) = fileAsset.run {
            FileAssetResponse(
                id!!,
                fileName,
                fileSize,
                type,
                status,
                hashId,
                uploadFolder,
                uploadFileName,
                date,
                isUploaded
            )
        }
    }
}

data class FileAssetUpdateRequest (
    var status: Status
)