package uz.davrbank.compliance

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import uz.davrbank.compliance.elasticsearch.ESHits
import java.time.LocalDate
import java.util.*

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var modifiedDate: Date? = null,
    @CreatedBy var createdBy: String? = null,
    @LastModifiedBy var modifiedBy: String? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false,
)

@Entity
class MyUser(
    @Column(unique = true) var username: String,
    var password: String,
    var firstname: String,
    var lastname: String,
    var patronymic: String?,
    @Enumerated(EnumType.STRING) var role: UserRole,
    var active: Boolean = true
) : BaseEntity() {

    fun fullName(): String {
        val fullName = StringBuilder()
        fullName.append("$firstname $lastname")
        patronymic?.let { fullName.append(" $it") }
        return fullName.toString()
    }
}


@Entity
class SparkUser(
    @Column(unique = true, nullable = false) var username: String,
    var password: String,
    var active: Boolean = true
) : BaseEntity()


@Entity
@Table(
    indexes = [
        Index(name = "hash_id_index", columnList = "hashId")
    ]
)
class FileAsset(
    var fileName: String,
    var fileSize: Long,
    @Column(unique = true) var hashId: String,
    var uploadFolder: String,
    var uploadFileName: String,
    @Enumerated(EnumType.STRING) var type: FileType,
    var isUploaded: Boolean = false,
    @Enumerated(EnumType.STRING) var status: Status,
    var date: LocalDate? = null,
) : BaseEntity()



@Entity
data class UserAuthorization(
    @Column(unique = true) val accessToken: String,
    @Column(unique = true) val refreshToken: String,
    private val accessTokenExpiresAt: Date,
    private val refreshTokenExpiresAt: Date,
    @ManyToOne val user: MyUser
) : BaseEntity() {

    fun isAccessTokenExpired() = accessTokenExpiresAt.time < Date().time

    fun isRefreshTokenExpired() = refreshTokenExpiresAt.time < Date().time

    fun accessTokenExpiresIn() = accessTokenExpiresAt.time - Date().time
}


// Absdan kelgan malumotlarni compliancedagi malumotlar bilan tekshirish
@Document(collection = "abs_data_logs")
class AbsDataChecker(
    var request: AbsDataCheckerRequest,
    var response: ESHits? = null,
    var status: AbsHandlerStatus = AbsHandlerStatus.CREATED,
    var complianceStatus: ComplianceStatus = ComplianceStatus.NOT_CHECKED,
    var operationType: AbsCheckOperationType,
    var comment: String? = null,
    @Indexed(expireAfter = "30d") val createdDate: Date = Date(),
    @org.springframework.data.annotation.Id var id: String? = null
)




// moderator kelgan requestni statusini o'zgartirgan vaqti complains da malumot qay holda turgan ekanligini saqlash uchun
@Document
class ComplianceCheckDataStore(
    var ids: List<String>,
    var absDataCheckerId: String,
    var tryCount: Long,
    var errorMessage: String? = null,
    @Indexed(expireAfter = "30d") val createdDate: Date = Date(),
    @org.springframework.data.annotation.Id var id: String? = null
)


@Document
class EsDataUpdater(
    @org.springframework.data.annotation.Id var id: String? = null,
    var isActive: Boolean = false,
)