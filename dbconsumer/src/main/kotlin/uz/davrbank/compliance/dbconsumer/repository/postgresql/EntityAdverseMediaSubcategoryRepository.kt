package uz.davrbank.compliance.dbconsumer.repository.postgresql

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uz.davrbank.compliance.dbconsumer.domain.postgresql.EntityAdverseMediaSubcategory

@Repository
interface EntityAdverseMediaSubcategoryRepository : JpaRepository<EntityAdverseMediaSubcategory, String>
