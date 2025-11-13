package co.bondspot.spbttest.domain.signature

import co.bondspot.spbttest.domain.entity.Task

interface TaskApplicationServiceSignature {
    fun create(task: Task): Task
    fun getById(id: String): Task?
    fun update(id: String, task: Task): Boolean?
    fun list(): List<Task>
}