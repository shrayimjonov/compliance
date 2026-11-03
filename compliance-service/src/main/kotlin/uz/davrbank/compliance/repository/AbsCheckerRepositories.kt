package uz.davrbank.compliance.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import uz.davrbank.compliance.domain.AbsCheckerRecord
import uz.davrbank.compliance.enums.AbsHandlerStatus

interface AbsCheckerRepository : MongoRepository<AbsCheckerRecord, String> {
    fun findAllByStatusInOrderByCreatedDateAsc(
        statuses: Collection<AbsHandlerStatus>,
        pageable: Pageable,
    ): Page<AbsCheckerRecord>
}
