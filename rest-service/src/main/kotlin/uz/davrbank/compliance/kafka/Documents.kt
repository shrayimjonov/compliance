package uz.davrbank.compliance.kafka

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*


@Document(collection = "entity_delete_data_store")
class EntityDeleteDataStore(
    @Indexed var complianceId: String,
    var deletedDate: String,
    var tryCount: Long = 0,
    var jobFinished: Boolean = false,
    var errorMessage: String? = null,
    @Indexed(expireAfter = "30d") val createdDate: Date = Date(),
    @org.springframework.data.annotation.Id var id: String? = null
)


@Document(collection = "topic")
class Topic(
    var topicName: String,
    var topic: Any,
    var topicType: String,
    var exceptionMessage: String,
    var exceptionDate: Date = Date(),
    var retryCount: Int = 0,
    @Id val id: String? = null,
)