package uz.davrbank.compliance.dbconsumer.repository.mongodb

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import uz.davrbank.compliance.dbconsumer.domain.mongodb.Topic

@Repository
interface TopicRepository : MongoRepository<Topic, String>
