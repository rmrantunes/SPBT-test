package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.contract.IFgaProvider
import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import co.bondspot.spbttest.domain.contract.INotificationObjectRepository
import co.bondspot.spbttest.domain.contract.INotificationRepository
import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.FgaRelTuple
import co.bondspot.spbttest.domain.entity.Notification
import co.bondspot.spbttest.domain.entity.NotificationObject
import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.domain.event.TaskNewEvent
import co.bondspot.spbttest.domain.event.TaskStatusUpdatedEvent
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

// TODO regarding notifications
//  create new notification & notification objects
//  after identifying the receivers -> owner only or any viewer
//  is the receivers amount a tradeoff? Like if it was 1M+?
//  publish send notification (to the receivers) event

class TaskEventsServiceTests {
    private lateinit var service: TaskEventsService
    private lateinit var fts: IFullTextSearchProvider
    private lateinit var fga: IFgaProvider
    private lateinit var notifRepo: INotificationRepository
    private lateinit var notifObjectRepo: INotificationObjectRepository

    @BeforeEach
    fun setup() {
        fts = spyk<IFullTextSearchProvider>()
        fga = spyk<IFgaProvider>()
        notifRepo = spyk<INotificationRepository>()
        notifObjectRepo = spyk<INotificationObjectRepository>()

        service = TaskEventsService(fga, fts, notifRepo, notifObjectRepo)
    }

    @Nested
    @DisplayName("TaskNewEvent")
    inner class TaskNewEventTests {
        @Test
        fun `call inner methods successfully`() {
            val accountId = UUID.randomUUID().toString()
            val task = Task("title", description = "desc", id = UUID.randomUUID().toString())

            service.handleTaskNewEvent(TaskNewEvent(task, accountId))

            verify(exactly = 1) {
                fga invoke
                    "writeRelationship" withArguments
                    listOf(
                        FgaRelTuple(
                            Account.ENTITY_NAME to accountId,
                            Task.FgaRelations.OWNER,
                            Task.ENTITY_NAME to task.id!!,
                        )
                    )
            }

            verify(exactly = 1) {
                fts invoke "index" withArguments listOf(Task.ENTITY_NAME, listOf(task))
            }
        }
    }

    @Nested
    @DisplayName("TaskStatusUpdatedEvent")
    inner class TaskStatusUpdatedEventTests {
        val accountId = UUID.randomUUID().toString()
        val task =
            Task(
                "title",
                description = "desc",
                id = UUID.randomUUID().toString(),
                status = Task.Status.IN_PROGRESS,
            )

        @Test
        fun `call inner methods successfully`() {
            val notifInput =
                Notification(
                    Notification.Type.TASK_STATUS_UPDATED,
                    accountId,
                    mapOf("newStatus" to task.status, "taskTitle" to task.title),
                )

            val notifCreated = notifInput.copy(id = UUID.randomUUID().toString())

            every { notifRepo.create(notifInput) } returns notifCreated

            val notifObjectsInput =
                listOf(
                    NotificationObject(
                        NotificationObject.Type.TRIGGER,
                        NotificationObject.Entity.ACCOUNT,
                        notificationId = notifCreated.id!!,
                        accountId = accountId,
                    ),
                    NotificationObject(
                        NotificationObject.Type.SUBJECT,
                        NotificationObject.Entity.TASK,
                        notificationId = notifCreated.id,
                        taskId = task.id,
                    ),
                )

            val notifObjectsCreated =
                notifObjectsInput.map { it.copy(id = UUID.randomUUID().toString()) }

            every { notifObjectRepo.createMany(notifObjectsInput) } returns notifObjectsCreated

            service.handleTaskUpdatedStatusEvent(TaskStatusUpdatedEvent(task, accountId))

            verify(exactly = 1) { notifRepo invoke "create" withArguments listOf(notifInput) }

            verify(exactly = 1) {
                notifObjectRepo invoke "createMany" withArguments listOf(notifObjectsInput)
            }

            // TODO register notification to new async job/queue, to:
            //  - get the notifications receivers
            //  - register NotificationObject type RECEIVER (to future control of reach/view status)
            //  - and forward to effective sending queue
        }
    }
}
