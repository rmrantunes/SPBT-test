package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.event.TaskNewEvent

interface ITaskEventsService {
    fun handleTaskNewEvent(e: TaskNewEvent)
}
