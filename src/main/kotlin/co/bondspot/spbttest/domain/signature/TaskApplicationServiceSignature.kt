package co.bondspot.spbttest.domain.signature

import co.bondspot.spbttest.domain.entity.Task

interface TaskApplicationServiceSignature {
    fun create(task: Task): Task
    fun getById(id: String): Task?
    fun updateDetails(id: String, title: String? = null): Boolean?
    fun updateStatus(id: String, status: Task.Status): Boolean?
    fun list(): List<Task>
}