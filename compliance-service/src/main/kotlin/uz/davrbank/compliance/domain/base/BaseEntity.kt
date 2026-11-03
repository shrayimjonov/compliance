package uz.davrbank.compliance.domain.base

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.util.Date

@MappedSuperclass
abstract class BaseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var createdDate: Date? = null,
    var modifiedDate: Date? = null,
    var createdBy: String? = null,
    var modifiedBy: String? = null,
    @Column(nullable = false)
    var deleted: Boolean = false,
)