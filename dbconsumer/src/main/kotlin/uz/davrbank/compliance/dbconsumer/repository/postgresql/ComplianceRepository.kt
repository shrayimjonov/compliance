package uz.davrbank.compliance.dbconsumer.repository.postgresql

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uz.davrbank.compliance.dbconsumer.domain.postgresql.Compliance

@Repository
interface ComplianceRepository : JpaRepository<Compliance, String>{

    @Query("SELECT c.id FROM Compliance c")
    fun findAllIds(pageable: Pageable): Page<String>

    @Query("""
        SELECT DISTINCT c FROM Compliance c 
        LEFT JOIN FETCH c.identifications
        LEFT JOIN FETCH c.countryAssociations
        LEFT JOIN FETCH c.entityDOBs
        WHERE c.id IN :ids
    """)
    fun findAllByIdInWithIdentifications(ids: List<String>): List<Compliance>
}
