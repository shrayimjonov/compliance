package uz.davrbank.compliance

import jakarta.transaction.Transactional
import kotlinx.coroutines.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import uz.davrbank.compliance.backup.*
import uz.davrbank.compliance.elasticsearch.ESHits
import uz.davrbank.compliance.security.username
import mu.KotlinLogging
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.security.crypto.password.PasswordEncoder
import uz.davrbank.compliance.elasticsearch.ESCheckService
import uz.davrbank.compliance.kafka.FileKafkaDto
import uz.davrbank.compliance.kafka.KafkaDataSender
import java.util.Date


interface UserService {
    fun me(): UserDto

    fun findById(userId: Long): UserResponse

    fun getAll(pageable: Pageable): Page<UserResponse>

    fun addUser(request: UserAdminRequest): UserResponse

    fun updateUser(id: Long, request: UserAdminRequest): UserResponse

    fun deleteUser(id: Long)

    interface InternalService { // used internally, should not be accessed from controllers
        fun findUserByUsername(username: String): MyUser
        fun findUserById(id: Long): MyUser
    }
}

interface SparkUserService {
    fun add(request: SparkUserRequest)
    fun update(id: Long, request: SparkUserRequest): SparkUserResponse
    fun delete(id: Long)
    fun getOne(id: Long): SparkUserResponse
    fun getAll(pageable: Pageable): Page<SparkUserResponse>
}



interface FileService {
    fun getAll(pageable: Pageable): Page<FileAssetAllResponse>
    fun getOne(id: Long): FileAssetResponse
    fun delete(id: Long)
    fun update(id: Long, request: FileAssetUpdateRequest): FileAssetResponse
    fun uploadData(id: Long)
}




@Service
class SparkUserServiceImpl(
    private val sparkUserRepository: SparkUserRepository, private val passwordEncoder: BCryptPasswordEncoder
) : SparkUserService {

    @Transactional
    override fun add(request: SparkUserRequest) {
        if (sparkUserRepository.existsByUsernameAndDeletedFalse(request.username)) {
            throw GeneralApiException("Username already exists")
        }
        val sparkUser = SparkUser(
            username = request.username, password = passwordEncoder.encode(request.password), active = request.active
        )
        sparkUserRepository.save(sparkUser)
    }

    @Transactional
    override fun update(id: Long, request: SparkUserRequest): SparkUserResponse {
        val existingUser =
            sparkUserRepository.findByIdAndDeletedFalse(id) ?: throw GeneralApiException("User not found")
        if (existingUser.username != request.username && sparkUserRepository.existsByUsernameAndDeletedFalse(request.username)) {
            throw GeneralApiException("Username already exists")
        }

        val updatedUser = existingUser.apply {
            username = request.username
            request.password?.let { password = passwordEncoder.encode(it) }
            active = request.active
        }

        val savedUser = sparkUserRepository.save(updatedUser)
        return SparkUserResponse(savedUser.id!!, savedUser.username, savedUser.active)
    }

    override fun delete(id: Long) {
        sparkUserRepository.deleteById(id)
    }

    override fun getOne(id: Long): SparkUserResponse {
        val user = sparkUserRepository.findByIdAndDeletedFalse(id) ?: throw GeneralApiException("User not found")
        return SparkUserResponse(user.id!!, user.username, user.active)
    }

    override fun getAll(pageable: Pageable): Page<SparkUserResponse> {
        val page = sparkUserRepository.findAllNotDeleted(pageable)
        return page.map { SparkUserResponse(it.id!!, it.username, it.active) }
    }

}


@Service
class UserServiceImpl(
    private val userRepository: UserRepository, private val passwordEncoder: BCryptPasswordEncoder
) : UserService, UserService.InternalService {

    override fun me(): UserDto {
        val me = findUserByUsername(username())
        return UserDto.toDto(me)
    }

    override fun findById(userId: Long): UserResponse {
        val user = findUserById(userId)
        return UserResponse.toDto(user)
    }

    override fun getAll(pageable: Pageable): Page<UserResponse> {
        return userRepository.findAllNotDeleted(pageable).map {
            UserResponse.toDto(it)
        }
    }

    @Transactional
    override fun addUser(request: UserAdminRequest): UserResponse {
        return request.run {
            if (password == null) throw GeneralApiException("password cannot be null")

            if (userRepository.existsByUsernameAndDeletedFalse(username)) {
                throw GeneralApiException("Username already exist")
            }


            UserResponse.toDto(
                userRepository.save(
                    MyUser(
                        username, passwordEncoder.encode(password), firstname, lastname, patronymic, role
                    )
                )
            )
        }
    }

    @Transactional
    override fun updateUser(id: Long, request: UserAdminRequest): UserResponse {
        val myUser = userRepository.findByIdAndDeletedFalse(id) ?: throw UserNotFoundException()

        val checkedUsername = request.username.takeIf { condition -> condition != myUser.username }?.let { username ->
            if (userRepository.existsByUsernameAndDeletedFalse(username)) {
                throw GeneralApiException("Username already exists")
            }
            username
        } ?: myUser.username

        return request.let { req ->
            UserResponse.toDto(
                userRepository.save(myUser.apply {
                    req.password?.let {
                        this.password = passwordEncoder.encode(it)
                    }
                    this.username = checkedUsername
                    this.firstname = req.firstname
                    this.lastname = req.lastname
                    this.patronymic = req.patronymic
                    this.role = req.role
                })
            )
        }
    }

    override fun deleteUser(id: Long) {
        val myUser = userRepository.findByIdAndDeletedFalse(id) ?: throw UserNotFoundException()
        userRepository.trash(myUser.id!!)
    }

    override fun findUserByUsername(username: String) =
        userRepository.findByUsernameAndDeletedFalse(username) ?: throw UserNotFoundException()

    override fun findUserById(id: Long) = userRepository.findByIdAndDeletedFalse(id) ?: throw UserNotFoundException()
}


@Service
class FileServiceImpl(
    private val fileAssetRepository: FileAssetRepository,
    private val kafkaDataSender: KafkaDataSender
) : FileService {
    override fun getAll(pageable: Pageable): Page<FileAssetAllResponse> {
        return fileAssetRepository.findAllByOrderByDate(pageable).map {
            FileAssetAllResponse.toDto(it)
        }
    }

    override fun getOne(id: Long) = FileAssetResponse.toDto(
        fileAssetRepository.findByIdAndDeletedFalse(id) ?: throw GeneralApiException("File not found")
    )

    override fun delete(id: Long) {
        fileAssetRepository.trash(id)
    }

    override fun update(id: Long, request: FileAssetUpdateRequest): FileAssetResponse {
        val fileAsset = fileAssetRepository.findByIdAndDeletedFalse(id) ?: throw GeneralApiException("File not found")

        val isUploaded = request.status == Status.COMPLETED

        return FileAssetResponse.toDto(
            fileAssetRepository.save(fileAsset.apply {
                this.isUploaded = isUploaded
                status = request.status
            })
        )
    }

    override fun uploadData(id: Long) {
        val fileAsset = fileAssetRepository.findByIdAndDeletedFalse(id) ?: throw GeneralApiException("File not found")
        kafkaDataSender.sendFileData(FileKafkaDto(fileAsset.hashId))
    }

}