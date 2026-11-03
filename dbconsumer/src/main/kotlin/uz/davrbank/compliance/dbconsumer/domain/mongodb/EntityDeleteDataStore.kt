package uz.davrbank.compliance.dbconsumer.domain.mongodb

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.util.Date

@Document(collection = "entity_delete_data_store")
class EntityDeleteDataStore(
    @Indexed var complianceId: String,
    var deletedDate: String,
    var tryCount: Long = 0,
    var jobFinished: Boolean = false,
    var errorMessage: String? = null,
    @Indexed(expireAfter = "30d") val createdDate: Date = Date(),
    @Id var id: String? = null
)
