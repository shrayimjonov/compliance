package uz.davrbank.compliance

import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import uz.davrbank.compliance.kafka.EntityDeleteDataStore

@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun trash(id: Long): T
    fun trashList(ids: List<Long>): List<T>
    fun findAllNotDeleted(pageable: Pageable): Page<T>
    fun findAllNotDeleted(): List<T>
    fun findByIdAndDeletedFalse(id: Long): T?
}

class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>,
    entityManager: EntityManager,
) : SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {
    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), false) }


    @Transactional
    override fun trash(id: Long) = save(findById(id).get().apply { deleted = true })
    override fun findAllNotDeleted(pageable: Pageable) = findAll(isNotDeletedSpecification, pageable)
    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)
    override fun findByIdAndDeletedFalse(id: Long) = findByIdOrNull(id)?.run { if (deleted) null else this }
    override fun trashList(ids: List<Long>): List<T> = ids.map { trash(it) }
}

interface UserRepository : BaseRepository<MyUser> {
    fun existsByUsernameAndDeletedFalse(username: String): Boolean
    fun findByUsernameAndDeletedFalse(username: String): MyUser?
}

interface UserAuthorizationRepository : BaseRepository<UserAuthorization> {
    fun findByRefreshTokenAndDeletedFalse(refreshToken: String): UserAuthorization?
    fun findByUserIdAndDeletedFalse(userId: Long): UserAuthorization?
    fun findByAccessTokenAndDeletedFalse(accessToken: String): UserAuthorization?
}


@Repository
interface SparkUserRepository : BaseRepository<SparkUser> {
    fun findByUsernameAndDeletedFalse(userName: String): SparkUser?
    fun existsByUsernameAndDeletedFalse(userName: String): Boolean
}

interface AbsDataCheckerRepository : MongoRepository<AbsDataChecker, String>{
    fun findAllByStatus(status: AbsHandlerStatus): List<AbsDataChecker>
    fun findAllByStatusOrderByCreatedDateDesc(status: AbsHandlerStatus, pageable: Pageable): Page<AbsDataChecker>
}

interface EntityDeleteDataStoreRepository : MongoRepository<EntityDeleteDataStore, String>{
    fun findAllByJobFinishedIsFalseOrderByCreatedDate(): List<EntityDeleteDataStore>
}



@Repository
interface AbsDataCheckerItemRepository : MongoRepository<AbsDataCheckerItem, String> {

}

@Repository
interface ComplianceCheckDataStoreRepository: MongoRepository<ComplianceCheckDataStore, String> {

}


@Repository
interface EsDataUpdaterRepository: MongoRepository<EsDataUpdater, String> {

}


@Repository
interface ApiLogRepository: MongoRepository<ApiLog, String> {
    fun findAllByCreatedDate(pageable: Pageable): Page<ApiLog>

}


@Repository
interface FileAssetRepository : BaseRepository<FileAsset> {
    fun findAllByOrderByDate(pageable: Pageable): Page<FileAsset>
}