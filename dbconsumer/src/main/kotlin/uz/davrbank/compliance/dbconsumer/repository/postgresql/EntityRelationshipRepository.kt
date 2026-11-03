package uz.davrbank.compliance.dbconsumer.repository.postgresql

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityRelationship

@Repository
interface EntityRelationshipRepository : JpaRepository<EntityRelationship, String> {
    fun findAllByRelatedEntityGUID(relatedEntityGUID: String): List<EntityRelationship>
}
