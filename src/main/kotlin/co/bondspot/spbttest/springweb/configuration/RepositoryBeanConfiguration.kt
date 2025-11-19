package co.bondspot.spbttest.springweb.configuration

import co.bondspot.spbttest.domain.contract.AccountRepositoryContract
import co.bondspot.spbttest.domain.contract.MessageRepositoryContract
import co.bondspot.spbttest.domain.contract.TaskRepositoryContract
import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.Message
import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.springweb.persistence.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.repository.findByIdOrNull

@Configuration
class RepositoryBeanConfiguration(
    private val accountRepository: AccountRepository,
    private val messageRepository: MessageRepository,
    private val taskRepository: TaskRepository
) {

    @Bean
    fun getMessageRepository(): MessageRepositoryContract = object : MessageRepositoryContract {
        override fun find(): List<Message> = messageRepository.findAll().map { it.toDomain() }

        override fun findById(id: String): Message? = messageRepository.findByIdOrNull(id)?.toDomain()

        override fun save(message: Message): Message =
            messageRepository.save(MessageEntity.fromDomain(message)).toDomain()
    }

    @Bean
    fun getTaskRepository(): TaskRepositoryContract = object : TaskRepositoryContract {
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

    @Bean
    fun getAccountRepository(): AccountRepositoryContract = object : AccountRepositoryContract {
        override fun register(account: Account): Account =
            accountRepository.save(AccountEntity.fromDomain(account)).toDomain()
        override fun getByEmail(email: String) = accountRepository.findByEmail(email)?.toDomain()
        override fun getByUsername(username: String): Account? = accountRepository.findByUsername(username)?.toDomain()
    }
}