package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.domain.signature.TaskRepositorySignature
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class TaskServiceTests {
    @Test
    fun `should create and return message`() {
        val repository = mockk<TaskRepositorySignature>()
        val created = Task("Text", "Some ID")
        every { repository.create(any()) } returns created
        val service = TaskService(repository)

        val result = service.create(created)

        Assertions.assertThat(result).isEqualTo(created)

    }
}