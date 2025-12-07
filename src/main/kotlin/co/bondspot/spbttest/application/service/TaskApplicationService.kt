package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.application.exception.ApplicationServiceException
import co.bondspot.spbttest.domain.contract.IAccountRepository
import co.bondspot.spbttest.domain.contract.IFgaProvider
import co.bondspot.spbttest.domain.contract.ITaskApplicationService
import co.bondspot.spbttest.domain.contract.ITaskRepository
import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.FgaRelationshipDef
import co.bondspot.spbttest.domain.entity.Task

open class TaskApplicationService(
    private val repository: ITaskRepository,
    private val accountRepository: IAccountRepository,
    private val fga: IFgaProvider,
) : ITaskApplicationService {
    override fun create(task: Task, reqAccount: Account): Task {
        // We're considering the user exists in the Api DB. Right approach? Prolly no lol

        return repository.create(task.copy(createdById = reqAccount.id)).also {
            // TODO if error thrown, remove created task, since no action could be done from any
            // account
            fga.writeRelationships(
                listOf(
                    FgaRelationshipDef(
                        "user" to reqAccount.id!!,
                        Task.FgaRelations.OWNER,
                        "task" to it.id!!,
                    )
                )
            )
        }
    }

    override fun getById(id: String, reqAccount: Account): Task {
        val task =
            repository.getById(id)
                ?: throw ApplicationServiceException("Task not found").setRelatedHttpStatusCode {
                    NOT_FOUND
                }

        if (
            !fga.checkRelationship(
                FgaRelationshipDef(
                    "user" to reqAccount.id!!,
                    Task.FgaRelations.VIEWER,
                    "task" to task.id!!,
                )
            )
        ) {
            throw ApplicationServiceException(
                    "Requested resource (Task: '$id') is not bonded to requester"
                )
                .setRelatedHttpStatusCode { FORBIDDEN }
        }

        //        if (task.createdById != reqAccount.id)
        //            throw ApplicationServiceException(
        //                    "Requested resource (Task: '$id') is not bonded to requester"
        //                )
        //                .setRelatedHttpStatusCode { FORBIDDEN }
        return task
    }

    override fun updateDetails(id: String, title: String?, reqAccount: Account): Boolean? {
        val existing = getById(id, reqAccount)
        repository.update(id, existing.copy(title = title ?: existing.title))
        return true
    }

    override fun updateStatus(id: String, status: Task.Status, reqAccount: Account): Boolean? {
        val existing = getById(id, reqAccount)
        repository.update(id, existing.copy(status = status))
        return true
    }

    override fun list(reqAccount: Account): List<Task> = repository.list()

    override fun shareWith(
        id: String,
        accountToShareWithId: String,
        relation: String?,
        reqAccount: Account,
    ): Boolean? {
        if (relation != Task.FgaRelations.VIEWER && relation != Task.FgaRelations.EDITOR) {
            // InternalApplicationServiceException
            throw ApplicationServiceException("Internal message: Unsupported relation for sharing")
                .setRelatedHttpStatusCode { INTERNAL_SERVER_ERROR }
        }

        val item = getById(id, reqAccount)

        if (
            !fga.checkRelationship(
                FgaRelationshipDef(
                    "user" to reqAccount.id!!,
                    Task.FgaRelations.OWNER,
                    "task" to item.id!!,
                )
            )
        ) {
            throw ApplicationServiceException("Requester missing owner relation to task")
                .setRelatedHttpStatusCode { FORBIDDEN }
        }

        // Here we're considering only accounts that already authenticated into the application
        accountRepository.getById(accountToShareWithId)
            ?: throw ApplicationServiceException("Account to share with not found")
                .setRelatedHttpStatusCode { NOT_FOUND }

        // TODO catch and throw normalized 5xx error
        // TODO if error thrown from FGA provider, register and alert
        fga.writeRelationships(
            listOf(
                FgaRelationshipDef(
                    "user" to accountToShareWithId,
                    relation,
                    "task" to id,
                )
            )
        )

        return true
    }
}
