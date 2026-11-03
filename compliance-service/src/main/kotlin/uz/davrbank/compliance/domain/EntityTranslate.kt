package uz.davrbank.compliance.domain

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import uz.davrbank.compliance.enums.Language
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "entity_translate"
)
class EntityTranslate(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "last_updated_date", nullable = false)
    var lastUpdatedDate: LocalDateTime,

    @Column(name = "original_text", nullable = false, columnDefinition = "TEXT")
    var originalText: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var translations: Map<Language, String> = emptyMap(),

    @Column(name = "entity_id", nullable = false)
    var entityId: UUID,

    @Column(name = "entity_type", nullable = false)
    var entityType: String,
)