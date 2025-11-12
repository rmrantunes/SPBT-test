package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.repository.MessageRepository
import co.bondspot.spbttest.domain.entity.Message
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class MessageService(private val repository: MessageRepository) {
    fun findMessages(): List<Message> = repository.findAll().toList()

    fun findMessageById(id: String): Message? = repository.findByIdOrNull(id)

    fun save(message: Message): Message = repository.save(message)
}