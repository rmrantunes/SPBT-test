package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.domain.contract.ITaskRepository
import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.Task
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TaskServiceTests {
    private val accountId = "accountId"
    private val reqAccount = Account("some_account", "some_email@example.com", id = accountId)

    @Nested
    @DisplayName("when creating a task...")
    inner class CreateTask {
        @Test
        fun `should create and return task`() {
            val repository = mockk<ITaskRepository>()
            val task = Task("Text", id = "Some ID")
            val createdTask = task.copy(createdById = task.id)
            every { repository.create(task) } returns createdTask
            val service = TaskService(repository)

            val result = service.create(task, reqAccount = reqAccount)

            Assertions.assertThat(result).isEqualTo(createdTask)
        }
    }

    @Test
    fun `should update status`() {
        val repository = spyk<ITaskRepository>(recordPrivateCalls = true)
        val id = "some_id"
        val existing = Task("Text", id = id)
        every { repository.create(any()) } returns existing
        val service = TaskService(repository)

        every { repository.getById(id) } returns existing

        val result = service.updateStatus(id, Task.Status.IN_PROGRESS, reqAccount)

        Assertions.assertThat(result).isTrue()

        verify { repository invoke "update" withArguments listOf(id, existing.copy(status = Task.Status.IN_PROGRESS)) }
    }

    @Test
    fun `should update details`() {
        val repository = spyk<ITaskRepository>(recordPrivateCalls = true)
        val id = "some_id"
        val existing = Task("Text", id = id)
        every { repository.create(any()) } returns existing
        val service = TaskService(repository)

        every { repository.getById(id) } returns existing

        val result = service.updateDetails(id, "Editado", reqAccount)

        Assertions.assertThat(result).isTrue()

        verify { repository invoke "update" withArguments listOf(id, existing.copy(title = "Editado")) }
    }
}