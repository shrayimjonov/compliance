package uz.davrbank.compliance.dbconsumer.domain.mongodb

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.Date

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
