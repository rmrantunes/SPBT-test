package co.bondspot.spbttest.springweb.configuration

import co.bondspot.spbttest.domain.contract.IAccountRepository
import co.bondspot.spbttest.domain.contract.ITaskRepository
import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.springweb.persistence.AccountEntity
import co.bondspot.spbttest.springweb.persistence.AccountRepository
import co.bondspot.spbttest.springweb.persistence.TaskEntity
import co.bondspot.spbttest.springweb.persistence.TaskRepository
import kotlin.jvm.optionals.getOrNull
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.repository.findByIdOrNull

@Configuration
class RepositoryBeanConfig(
    private val accountRepository: AccountRepository,
    private val taskRepository: TaskRepository,
) {
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

            override fun listByIds(ids: List<String>): List<Task> {
                TODO("Not yet implemented")
            }
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
