package co.bondspot.spbttest.springweb.configuration

import co.bondspot.spbttest.domain.signature.MessageRepositorySignature
import co.bondspot.spbttest.domain.entity.Message
import co.bondspot.spbttest.springweb.persistence.MessageEntity
import co.bondspot.spbttest.springweb.persistence.MessageRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.repository.findByIdOrNull

@Configuration
class RepositoryBeanConfiguration(private val messageRepository: MessageRepository) {

    @Bean
    fun getMessageRepository(): MessageRepositorySignature = object : MessageRepositorySignature {
        override fun find(): List<Message> = messageRepository.findAll().map { it.toDomain() }

        override fun findById(id: String): Message? = messageRepository.findByIdOrNull(id)?.toDomain()

        override fun save(message: Message): Message =
            messageRepository.save(MessageEntity.fromDomain(message)).toDomain()
    }
}