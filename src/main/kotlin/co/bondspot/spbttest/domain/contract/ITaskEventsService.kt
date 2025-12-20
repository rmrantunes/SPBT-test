package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.event.TaskNewEvent
import co.bondspot.spbttest.domain.event.TaskSharedEvent
import co.bondspot.spbttest.domain.event.TaskSharingRevokedEvent
import co.bondspot.spbttest.domain.event.TaskStatusUpdatedEvent

interface ITaskEventsService {
    fun handleTaskNewEvent(e: TaskNewEvent)

    fun handleTaskUpdatedStatusEvent(e: TaskStatusUpdatedEvent)

    fun handleTaskSharedEvent(e: TaskSharedEvent)

    fun handleTaskSharingRevokedEvent(e: TaskSharingRevokedEvent)
}
