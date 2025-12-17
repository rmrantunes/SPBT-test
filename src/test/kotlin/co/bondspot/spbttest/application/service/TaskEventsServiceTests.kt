package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.domain.event.TaskNewEvent
import java.util.UUID
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TaskEventsServiceTests {
    @Nested
    @DisplayName("Create new notification for a task")
    inner class CreateTaskNotification {
        @Test
        fun `not implemented`() {
            val service = TaskEventsService()

            // create new notification & notification objects
            // after identifying the receivers -> owner only or any viewer
            // is the receivers amount a tradeoff? Like if it was 1M+?
            // publish send notification (to the receivers) event

            assertThrows<NotImplementedError> {
                service.handleTaskNewEvent(
                    TaskNewEvent(Task(), UUID.randomUUID().toString())
                )
            }
        }
    }
}
