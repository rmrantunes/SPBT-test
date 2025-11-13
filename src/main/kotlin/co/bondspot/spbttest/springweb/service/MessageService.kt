package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.application.MessageApplicationService
import co.bondspot.spbttest.domain.contract.MessageRepositoryContract
import co.bondspot.spbttest.domain.entity.Message
import co.bondspot.spbttest.springweb.persistence.MessageEntity
import co.bondspot.spbttest.springweb.persistence.MessageRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class MessageService(private val repository: MessageRepository) :
    MessageApplicationService(
        object : MessageRepositoryContract {
            override fun find(): List<Message> = repository.findAll().map { it.toDomain() }

            override fun findById(id: String): Message? = repository.findByIdOrNull(id)?.toDomain()

            override fun save(message: Message): Message = repository.save(MessageEntity.fromDomain(message)).toDomain()
        }
    )