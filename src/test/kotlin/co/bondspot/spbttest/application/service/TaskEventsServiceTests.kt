package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.contract.IFgaProvider
import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.FgaRelTuple
import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.domain.event.TaskNewEvent
import io.mockk.spyk
import io.mockk.verify
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TaskEventsServiceTests {
    private lateinit var service: TaskEventsService
    private lateinit var fts: IFullTextSearchProvider
    private lateinit var fga: IFgaProvider

    @BeforeEach
    fun setup() {
        fts = spyk<IFullTextSearchProvider>()
        fga = spyk<IFgaProvider>()

        service = TaskEventsService(fga, fts)
    }

    @Nested
    @DisplayName("Create new notification for a task")
    inner class CreateTaskNotification {
        @Test
        fun `not implemented`() {
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

    /*
    TODO: regarding notifications
     create new notification & notification objects
     after identifying the receivers -> owner only or any viewer
     is the receivers amount a tradeoff? Like if it was 1M+?
     publish send notification (to the receivers) event
    */
}
