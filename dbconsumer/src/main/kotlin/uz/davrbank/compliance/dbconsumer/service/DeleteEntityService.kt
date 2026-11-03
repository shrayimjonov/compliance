package uz.davrbank.compliance.dbconsumer.service

import uz.davrbank.compliance.dbconsumer.dto.kafka.DeleteEventKafka

interface DeleteEntityService {
    fun saveDeleteEvent(message: DeleteEventKafka)
    fun processDeleteJob()
}
