package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.entity.Notification
import co.bondspot.spbttest.domain.event.NotificationNewEvent
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NotificationEventsServiceTests {
    val service by lazy { NotificationEventsService() }

    @Nested
    inner class NotificationNewTests {
        @Test
        fun `should handle notification new`() {
            assertThrows<NotImplementedError> {
                service.handle(
                    NotificationNewEvent(Notification(Notification.Type.TASK_STATUS_UPDATED))
                )
            }
        }
    }
}
