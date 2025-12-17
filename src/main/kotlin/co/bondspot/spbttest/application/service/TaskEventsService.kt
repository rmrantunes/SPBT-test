package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.contract.IFgaProvider
import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import co.bondspot.spbttest.domain.contract.ITaskEventsService
import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.FgaRelTuple
import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.domain.event.TaskNewEvent

class TaskEventsService(private val fga: IFgaProvider, private val fts: IFullTextSearchProvider) :
    ITaskEventsService {
    override fun handleTaskNewEvent(e: TaskNewEvent) {
        // TODO if error thrown in this block, remove created task, since no action could be
        //  done from any account.

        fga.writeRelationship(
            FgaRelTuple(
                Account.ENTITY_NAME to e.triggerAccountId,
                Task.FgaRelations.OWNER,
                Task.ENTITY_NAME to e.task.id!!,
            )
        )

        fts.index(Task.ENTITY_NAME, listOf(e.task))
    }
}
