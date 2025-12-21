package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.event.TaskNewEvent
import co.bondspot.spbttest.domain.event.TaskSharedEvent
import co.bondspot.spbttest.domain.event.TaskSharingRevokedEvent
import co.bondspot.spbttest.domain.event.TaskStatusUpdatedEvent

interface ITaskEventsService {
    fun handle(e: TaskNewEvent)

    fun handle(e: TaskStatusUpdatedEvent)

    fun handle(e: TaskSharedEvent)

    fun handle(e: TaskSharingRevokedEvent)
}
