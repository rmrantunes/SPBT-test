package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.application.exception.ApplicationServiceException
import co.bondspot.spbttest.domain.contract.ITaskApplicationService
import co.bondspot.spbttest.domain.contract.ITaskRepository
import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.Task

open class TaskApplicationService(private val repository: ITaskRepository) :
    ITaskApplicationService {
    override fun create(task: Task, reqAccount: Account): Task {
        // We're considering the user exists in the Api DB. Right approach?
        return repository.create(task.copy(createdById = reqAccount.id))
    }

    override fun getById(id: String, reqAccount: Account): Task? {
        val task =
            repository.getById(id)
                ?: throw ApplicationServiceException("Task not found").setRelatedHttpStatusCode {
                    NOT_FOUND
                }

        if (task.createdById != reqAccount.id)
            throw ApplicationServiceException(
                    "Requested resource (Task: '$id') is not bonded to requester"
                )
                .setRelatedHttpStatusCode { FORBIDDEN }
        return task
    }

    override fun updateDetails(id: String, title: String?, reqAccount: Account): Boolean? {
        val existing = getById(id, reqAccount) ?: return null
        repository.update(id, existing.copy(title = title ?: existing.title))
        return true
    }

    override fun updateStatus(id: String, status: Task.Status, reqAccount: Account): Boolean? {
        val existing = getById(id, reqAccount) ?: return null
        repository.update(id, existing.copy(status = status))
        return true
    }

    override fun list(reqAccount: Account): List<Task> = repository.list()
}
