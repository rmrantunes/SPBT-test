package co.bondspot.spbttest.springweb.configuration

import co.bondspot.spbttest.domain.entity.Message
import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.domain.signature.MessageRepositorySignature
import co.bondspot.spbttest.domain.signature.TaskRepositorySignature
import co.bondspot.spbttest.springweb.persistence.MessageEntity
import co.bondspot.spbttest.springweb.persistence.MessageRepository
import co.bondspot.spbttest.springweb.persistence.TaskEntity
import co.bondspot.spbttest.springweb.persistence.TaskRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.repository.findByIdOrNull

@Configuration
class RepositoryBeanConfiguration(
    private val messageRepository: MessageRepository,
    private val taskRepository: TaskRepository
) {

    @Bean
    fun getMessageRepository(): MessageRepositorySignature = object : MessageRepositorySignature {
        override fun find(): List<Message> = messageRepository.findAll().map { it.toDomain() }

        override fun findById(id: String): Message? = messageRepository.findByIdOrNull(id)?.toDomain()

        override fun save(message: Message): Message =
            messageRepository.save(MessageEntity.fromDomain(message)).toDomain()
    }

    @Bean
    fun getTaskRepository(): TaskRepositorySignature = object : TaskRepositorySignature {
        override fun create(task: Task): Task = taskRepository.save(TaskEntity.fromDomain(task)).toDomain()

        override fun getById(id: String): Task? = taskRepository.findByIdOrNull(id)?.toDomain()

        override fun update(id: String, task: Task): Boolean? {
            val existing = taskRepository.findByIdOrNull(id)
            return if (existing == null) null
            else {
                taskRepository.save(TaskEntity.fromDomain(task))
                true
            }
        }

        override fun list(): List<Task> = taskRepository.findAll().map { it.toDomain() }

    }
}