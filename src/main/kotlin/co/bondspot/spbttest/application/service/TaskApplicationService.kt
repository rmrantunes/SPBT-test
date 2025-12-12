package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.application.exception.ApplicationServiceException
import co.bondspot.spbttest.application.exception.ApplicationServiceInternalException
import co.bondspot.spbttest.domain.contract.IAccountRepository
import co.bondspot.spbttest.domain.contract.IFgaProvider
import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import co.bondspot.spbttest.domain.contract.ITaskApplicationService
import co.bondspot.spbttest.domain.contract.ITaskRepository
import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.FTS_DEFAULT_PRIMARY_KEY
import co.bondspot.spbttest.domain.entity.FgaRelTuple
import co.bondspot.spbttest.domain.entity.Task

open class TaskApplicationService(
    private val taskRepo: ITaskRepository,
    private val accountRepo: IAccountRepository,
    private val fga: IFgaProvider,
    private val fts: IFullTextSearchProvider,
) : ITaskApplicationService {
    override fun create(task: Task, reqAccount: Account): Task {
        // We're considering the user exists in the Api DB. Right approach? Prolly no lol

        return taskRepo.create(task.copy(createdById = reqAccount.id)).also {
            // TODO if error thrown in this block, remove created task, since no action could be
            //  done from any account.
            // TODO Consider adding a listener handler (including this block) to react to task
            //  creation event and optimize this service.
            fga.writeRelationship(
                FgaRelTuple(
                    Account.ENTITY_NAME to reqAccount.id!!,
                    Task.FgaRelations.OWNER,
                    Task.ENTITY_NAME to it.id!!,
                )
            )

            // TODO normalize errors FtsProviderException
            fts.index(Task.ENTITY_NAME, listOf(it))
        }
    }

    override fun getById(id: String, reqAccount: Account): Task {
        val task =
            taskRepo.getById(id)
                ?: throw ApplicationServiceException("Task not found").setRelatedHttpStatusCode {
                    NOT_FOUND
                }

        if (
            !fga.checkRelationship(
                FgaRelTuple(
                    Account.ENTITY_NAME to reqAccount.id!!,
                    Task.FgaRelations.VIEWER,
                    Task.ENTITY_NAME to task.id!!,
                )
            )
        ) {
            throw ApplicationServiceException("Requested resource is not bonded to requester")
                .setRelatedHttpStatusCode { FORBIDDEN }
        }

        return task
    }

    override fun updateDetails(id: String, title: String?, reqAccount: Account): Boolean? {
        val existing = getById(id, reqAccount)

        if (
            !fga.checkRelationship(
                FgaRelTuple(
                    Account.ENTITY_NAME to reqAccount.id!!,
                    Task.FgaRelations.CAN_EDIT_DETAILS,
                    Task.ENTITY_NAME to existing.id!!,
                )
            )
        ) {
            throw ApplicationServiceException(
                    "Requester does not have sufficient permission to perform this action"
                )
                .setRelatedHttpStatusCode { FORBIDDEN }
        }

        val updated = existing.copy(title = title ?: existing.title)

        taskRepo.update(id, updated)

        // TODO normalize errors FtsProviderException
        fts.index(Task.ENTITY_NAME, listOf(updated))
        return true
    }

    override fun updateStatus(id: String, status: Task.Status, reqAccount: Account): Boolean? {
        val existing = getById(id, reqAccount)

        if (
            !fga.checkRelationship(
                FgaRelTuple(
                    Account.ENTITY_NAME to reqAccount.id!!,
                    Task.FgaRelations.CAN_EDIT_STATUS,
                    Task.ENTITY_NAME to existing.id!!,
                )
            )
        ) {
            throw ApplicationServiceException(
                    "Requester does not have sufficient permission to perform this action"
                )
                .setRelatedHttpStatusCode { FORBIDDEN }
        }

        val updated = existing.copy(status = status)

        taskRepo.update(id, updated)

        // TODO normalize errors FtsProviderException
        fts.index(Task.ENTITY_NAME, listOf(updated))
        return true
    }

    override fun list(ftsTerm: String?, reqAccount: Account): List<Task> {
        val relatedObjects =
            fga.listObjects(
                Account.ENTITY_NAME to reqAccount.id!!,
                Task.FgaRelations.VIEWER,
                Task.ENTITY_NAME,
            )

        if (relatedObjects.isEmpty()) return emptyList()

        return if (ftsTerm != null) {
            val searchResultIds =
                fts.search(
                        collection = Task.ENTITY_NAME,
                        query = ftsTerm,
                        ids = relatedObjects.map { it.second },
                    )
                    .hits
                    .mapNotNull { it[FTS_DEFAULT_PRIMARY_KEY] as? String }

            taskRepo.listByIds(searchResultIds)
        } else {
            taskRepo.listByIds(relatedObjects.map { it.second })
        }
    }

    override fun shareWith(
        id: String,
        accountToShareWithId: String,
        relation: String?,
        reqAccount: Account,
    ): Boolean? {
        if (relation != Task.FgaRelations.VIEWER && relation != Task.FgaRelations.EDITOR) {
            throw ApplicationServiceInternalException("Unsupported relation for sharing")
        }

        val item = getById(id, reqAccount)

        if (
            !fga.checkRelationship(
                FgaRelTuple(
                    Account.ENTITY_NAME to reqAccount.id!!,
                    Task.FgaRelations.OWNER,
                    Task.ENTITY_NAME to item.id!!,
                )
            )
        ) {
            throw ApplicationServiceException("Requester missing owner relation to task")
                .setRelatedHttpStatusCode { FORBIDDEN }
        }

        // Here we're considering only accounts that already authenticated into the application
        accountRepo.getById(accountToShareWithId)
            ?: throw ApplicationServiceException("Account to share with not found")
                .setRelatedHttpStatusCode { NOT_FOUND }

        // TODO catch and throw normalized 5xx error
        // TODO if error thrown from FGA provider, register and alert
        fga.writeRelationship(
            FgaRelTuple(
                Account.ENTITY_NAME to accountToShareWithId,
                relation,
                Task.ENTITY_NAME to id,
            )
        )

        return true
    }
}
