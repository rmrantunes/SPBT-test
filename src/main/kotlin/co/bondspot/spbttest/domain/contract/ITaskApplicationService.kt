package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.Task

interface ITaskApplicationService {
    /**
     * Creates a new task.
     *
     * The created task is bonded to the requester account, making it the resource owner.
     *
     * Inside this method is also created the FGA rule for the owner-resource relationship.
     */
    fun create(task: Task, reqAccount: Account): Task

    /**
     * Returns the task by id if the requester account is bonded to it in any way.
     *
     * Throws forbidden error if not.
     */
    fun getById(id: String, reqAccount: Account): Task?

    /**
     * Updates the task details by id if the requester account is bonded to it in any way.
     *
     * Throws forbidden error if not.
     */
    fun updateDetails(id: String, title: String? = null, reqAccount: Account): Boolean?

    /**
     * Updates the task status by id if the requester account is bonded to it in any way.
     *
     * Throws forbidden error if not.
     */
    fun updateStatus(id: String, status: Task.Status, reqAccount: Account): Boolean?

    /** Lists the tasks that the requester account is bonded to in any way. */
    fun list(queryTerm: String? = null, reqAccount: Account): List<Task>

    fun shareWith(
        id: String,
        accountIdToShareWith: String,
        relation: String? = "viewer",
        reqAccount: Account,
    ): Boolean?

    fun listRelatedAccounts(id: String, reqAccount: Account)
}
