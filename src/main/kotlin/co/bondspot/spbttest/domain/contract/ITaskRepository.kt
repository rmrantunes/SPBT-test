package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.Task

interface ITaskRepository {
    fun create(task: Task): Task
    fun getById(id: String): Task?
    fun update(id: String, task: Task): Boolean?
    fun list(): List<Task>
}