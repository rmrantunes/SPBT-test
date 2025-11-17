package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.application.exception.ApplicationServiceException
import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.domain.contract.TaskApplicationServiceContract
import co.bondspot.spbttest.domain.contract.TaskRepositoryContract

open class TaskApplicationService(
    private val repository: TaskRepositoryContract
) : TaskApplicationServiceContract {
    override fun create(task: Task): Task = repository.create(task)

    /**
     * @throws co.bondspot.spbttest.application.exception.ApplicationServiceException
     * */
    override fun getById(id: String): Task? {
        return repository.getById(id) ?: throw ApplicationServiceException("Task not found")
            .relatedHttpStatusCode {
            NOT_FOUND
        }
    }

    override fun updateDetails(id: String, title: String?): Boolean? {
        val existing = getById(id) ?: return null
        repository.update(
            id,
            existing.copy(
                title = title ?: existing.title,
            )
        )
        return true
    }

    override fun updateStatus(id: String, status: Task.Status): Boolean? {
        val existing = getById(id) ?: return null
        repository.update(
            id,
            existing.copy(
                status = status,
            )
        )
        return true
    }

    override fun list(): List<Task> = repository.list()

}