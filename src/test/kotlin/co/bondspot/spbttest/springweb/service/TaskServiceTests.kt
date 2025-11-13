package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.domain.signature.TaskRepositorySignature
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class TaskServiceTests {

    @Test
    fun `should create and return message`() {
        val repository = mockk<TaskRepositorySignature>()
        val created = Task("Text", id = "Some ID")
        every { repository.create(any()) } returns created
        val service = TaskService(repository)

        val result = service.create(created)

        Assertions.assertThat(result).isEqualTo(created)
    }

    @Test
    fun `should update status`() {
        val repository = spyk<TaskRepositorySignature>(recordPrivateCalls = true)
        val id = "some_id"
        val existing = Task("Text", id = id)
        every { repository.create(any()) } returns existing
        val service = TaskService(repository)

        every { repository.getById(id) } returns existing

        val result = service.updateStatus(id, Task.Status.IN_PROGRESS)

        Assertions.assertThat(result).isTrue()

        verify { repository invoke "update" withArguments listOf(id, existing.copy(status = Task.Status.IN_PROGRESS)) }
    }

    @Test
    fun `should update details`() {
        val repository = spyk<TaskRepositorySignature>(recordPrivateCalls = true)
        val id = "some_id"
        val existing = Task("Text", id = id)
        every { repository.create(any()) } returns existing
        val service = TaskService(repository)

        every { repository.getById(id) } returns existing

        val result = service.updateDetails(id, "Editado")

        Assertions.assertThat(result).isTrue()

        verify { repository invoke "update" withArguments listOf(id, existing.copy(title = "Editado")) }
    }
}