package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.Task

interface ITaskApplicationService {
    fun create(task: Task, reqAccount: Account): Task
    fun getById(id: String): Task?
    fun updateDetails(id: String, title: String? = null): Boolean?
    fun updateStatus(id: String, status: Task.Status): Boolean?
    fun list(): List<Task>
}