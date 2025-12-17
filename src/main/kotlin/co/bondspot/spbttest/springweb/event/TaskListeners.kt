package co.bondspot.spbttest.springweb.event

import co.bondspot.spbttest.domain.contract.ITaskEventsService
import co.bondspot.spbttest.domain.event.TaskNewEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class TaskListeners {
    @Autowired private lateinit var taskEventsService: ITaskEventsService

    @EventListener
    fun handleTaskNewEvent(event: TaskNewEvent) {
        taskEventsService.handleTaskNewEvent(event)
    }
}
