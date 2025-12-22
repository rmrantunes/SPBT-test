package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import co.bondspot.spbttest.domain.contract.INotificationObjectRepository
import co.bondspot.spbttest.domain.entity.FtsSearchResponse
import co.bondspot.spbttest.domain.entity.Notification
import co.bondspot.spbttest.domain.entity.NotificationObject
import co.bondspot.spbttest.domain.event.NotificationNewEvent
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class NotificationEventsServiceTests {
    lateinit var service: NotificationEventsService
    lateinit var fts: IFullTextSearchProvider
    lateinit var notifObjectRepo: INotificationObjectRepository

    @BeforeEach
    fun setup() {
        fts = spyk<IFullTextSearchProvider>(recordPrivateCalls = true)
        notifObjectRepo = spyk<INotificationObjectRepository>(recordPrivateCalls = true)

        service = NotificationEventsService(notifObjectRepo, fts)
    }



    val accountId = UUID.randomUUID().toString()
    val accountId2 = UUID.randomUUID().toString()
    val taskId = UUID.randomUUID().toString()

    val notification =
        Notification(Notification.Type.TASK_STATUS_UPDATED, actionTriggerAccountId = accountId, id = UUID.randomUUID().toString())
    val objects =
        listOf(
            NotificationObject(
                type = NotificationObject.Type.TRIGGER,
                entity = NotificationObject.Entity.ACCOUNT,
                notificationId = notification.id!!,
                accountId = accountId,
            ),
            NotificationObject(
                type = NotificationObject.Type.SUBJECT,
                entity = NotificationObject.Entity.TASK,
                notificationId = notification.id,
                taskId = taskId,
            ),
        )

    @Nested
    inner class NotificationNewTests {
        @Test
        fun `should handle notification new`() {
            val hits = listOf(mapOf("accountId" to accountId), mapOf("accountId" to accountId2))
            every { fts.search("task", "", ids = listOf(taskId)) } returns FtsSearchResponse(hits)

            service.handle(NotificationNewEvent(notification, objects))

            // Get possible accounts subscribed to the event
            // Create Notification Objects of type RECEIVER

            verify(exactly = 1) {
                notifObjectRepo invoke
                    "createMany" withArguments
                    listOf(
                        listOf(
                            NotificationObject(
                                type = NotificationObject.Type.RECEIVER,
                                entity = NotificationObject.Entity.ACCOUNT,
                                notificationId = notification.id!!,
                                accountId = accountId2,
                            )
                        )
                    )
            }

            // (future) add job task to deliverance queue for each receiver
        }
    }
}
