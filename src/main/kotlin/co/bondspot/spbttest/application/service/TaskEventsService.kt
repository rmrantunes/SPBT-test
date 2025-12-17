package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.contract.ITaskEventsService
import co.bondspot.spbttest.domain.event.TaskNewEvent

class TaskEventsService : ITaskEventsService {
    override fun handleTaskNewEvent(e: TaskNewEvent) {
        TODO("Not yet implemented")
    }
}
