package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.domain.signature.TaskApplicationServiceSignature
import co.bondspot.spbttest.domain.signature.TaskRepositorySignature

open class TaskApplicationService(
    private val repository: TaskRepositorySignature
) : TaskApplicationServiceSignature {
    override fun create(task: Task): Task = repository.create(task)

    override fun getById(id: String): Task? = repository.getById(id)

    override fun update(id: String, task: Task): Boolean? = repository.update(id, task)

    override fun list(): List<Task> = repository.list()

}