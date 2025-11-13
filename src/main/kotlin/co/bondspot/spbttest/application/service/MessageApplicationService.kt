package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.contract.MessageApplicationServiceContract
import co.bondspot.spbttest.infrastructure.repository.MessageRepository
import co.bondspot.spbttest.domain.entity.Message
import co.bondspot.spbttest.infrastructure.entity.MessageEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class MessageApplicationService(private val repository: MessageRepository) : MessageApplicationServiceContract {
    override fun find(): List<Message> = repository.findAll().toList().map { it.toDomainEntity() }

    override fun findById(id: String): Message? = repository.findByIdOrNull(id)?.toDomainEntity()

    override fun save(message: Message): Message = repository.save(MessageEntity.fromDomainEntity(message)).toDomainEntity()
}