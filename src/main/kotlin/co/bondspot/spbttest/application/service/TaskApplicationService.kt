package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.domain.signature.TaskApplicationServiceSignature
import co.bondspot.spbttest.domain.signature.TaskRepositorySignature

open class TaskApplicationService(
    private val repository: TaskRepositorySignature
) : TaskApplicationServiceSignature {
    override fun create(task: Task): Task = repository.create(task)

    override fun getById(id: String): Task? = repository.getById(id)

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
        val existing = getById(id)?: return null
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