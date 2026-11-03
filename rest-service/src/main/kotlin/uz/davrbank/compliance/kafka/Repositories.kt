package uz.davrbank.compliance.kafka

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository


@Repository
interface TopicRepository : MongoRepository<Topic, String> {
    fun findAllByTopicNameAndRetryCountLessThanOrderByExceptionDateAsc(
        topicName: String, retryCount: Int, pageable: Pageable,
    ): Page<Topic>
}