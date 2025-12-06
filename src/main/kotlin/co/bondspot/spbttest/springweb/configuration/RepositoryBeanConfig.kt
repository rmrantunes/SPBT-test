package co.bondspot.spbttest.springweb.configuration

import co.bondspot.spbttest.domain.contract.IAccountRepository
import co.bondspot.spbttest.domain.contract.IMessageRepository
import co.bondspot.spbttest.domain.contract.ITaskRepository
import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.Message
import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.springweb.persistence.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.repository.findByIdOrNull
import kotlin.jvm.optionals.getOrNull

@Configuration
class RepositoryBeanConfig(
    private val accountRepository: AccountRepository,
    private val messageRepository: MessageRepository,
    private val taskRepository: TaskRepository,
) {

    @Bean
    fun getMessageRepository(): IMessageRepository =
        object : IMessageRepository {
            override fun find(): List<Message> = messageRepository.findAll().map { it.toDomain() }

            override fun findById(id: String): Message? =
                messageRepository.findByIdOrNull(id)?.toDomain()

            override fun save(message: Message): Message =
                messageRepository.save(MessageEntity.fromDomain(message)).toDomain()
        }

    @Bean
    fun getTaskRepository(): ITaskRepository =
        object : ITaskRepository {
            override fun create(task: Task): Task =
                taskRepository.save(TaskEntity.fromDomain(task)).toDomain()

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
    fun getAccountRepository(): IAccountRepository =
        object : IAccountRepository {
            override fun register(account: Account): Account =
                accountRepository.save(AccountEntity.fromDomain(account)).toDomain()

            override fun getByEmail(email: String) =
                accountRepository.findByEmail(email)?.toDomain()

            override fun getByUsername(username: String): Account? =
                accountRepository.findByUsername(username)?.toDomain()

            override fun getById(id: String): Account? =
                accountRepository.findById(id).getOrNull()?.toDomain()
        }
}
