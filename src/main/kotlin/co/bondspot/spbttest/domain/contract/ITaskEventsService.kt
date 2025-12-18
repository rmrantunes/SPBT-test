package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.event.TaskNewEvent
import co.bondspot.spbttest.domain.event.TaskUpdatedStatusEvent

interface ITaskEventsService {
    fun handleTaskNewEvent(e: TaskNewEvent)

    fun handleTaskUpdatedStatusEvent(e: TaskUpdatedStatusEvent)
}
