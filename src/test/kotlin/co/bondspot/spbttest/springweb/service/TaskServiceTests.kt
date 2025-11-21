package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.application.exception.ApplicationServiceException
import co.bondspot.spbttest.domain.contract.ITaskRepository
import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.shared.enumeration.HttpStatusCode
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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

    @Nested
    @DisplayName("when getting a task...")
    inner class GetTask {
        @Test
        fun `throw forbidden if req user is not bonded to task`() {
            val repository = spyk<ITaskRepository>(recordPrivateCalls = true)
            val id = "some_id"
            val existing = Task("Text", id = id)
            every { repository.create(any()) } returns existing.copy(id = accountId)
            val service = TaskService(repository)

            every { repository.getById(id) } returns existing.copy(createdById = "not_you")

            val ex = assertThrows<ApplicationServiceException> { service.getById(id, reqAccount) }
            assertThat(ex.message).isEqualTo("Requested resource (Task: '$id') is not bonded to requester")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.FORBIDDEN)
        }

        @Test
        fun `return task bonded to requester`() {
            val repository = spyk<ITaskRepository>(recordPrivateCalls = true)
            val id = "some_id"
            val existing = Task("Text", id = id)
            val createdTask = existing.copy(createdById = reqAccount.id)
            every { repository.create(any()) } returns createdTask
            val service = TaskService(repository)

            every { repository.getById(id) } returns createdTask

            val task = service.getById(id, reqAccount)
            assertThat(task).isEqualTo(createdTask)
        }
    }

    @Nested
    @DisplayName("when updating status...")
    inner class UpdateStatus {
        @Test
        fun `throw forbidden if req user is not bonded to task`() {
            val repository = spyk<ITaskRepository>(recordPrivateCalls = true)
            val id = "some_id"
            val existing = Task("Text", id = id)
            every { repository.create(any()) } returns existing.copy(id = accountId)
            val service = TaskService(repository)

            every { repository.getById(id) } returns existing.copy(createdById = "not_you")

            val ex = assertThrows<ApplicationServiceException> { service.updateStatus(id, Task.Status.IN_PROGRESS, reqAccount) }
            assertThat(ex.message).isEqualTo("Requested resource (Task: '$id') is not bonded to requester")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.FORBIDDEN)
        }

        @Test
        fun `should update successfully`() {
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
    }

    @Nested
    @DisplayName("when updating details...")
    inner class UpdateDetails {
        @Test
        fun `throw forbidden if req user is not bonded to task`() {
            val repository = spyk<ITaskRepository>(recordPrivateCalls = true)
            val id = "some_id"
            val existing = Task("Text", id = id)
            every { repository.create(any()) } returns existing.copy(id = accountId)
            val service = TaskService(repository)

            every { repository.getById(id) } returns existing.copy(createdById = "not_you")

            val ex = assertThrows<ApplicationServiceException> { service.updateDetails(id, "Editado", reqAccount) }
            assertThat(ex.message).isEqualTo("Requested resource (Task: '$id') is not bonded to requester")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.FORBIDDEN)
        }

        @Test
        fun `should update successfully`() {
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
}