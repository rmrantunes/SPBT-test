package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.entity.Message
import co.bondspot.spbttest.domain.repository.MessageRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test


class MessageServiceTests {

    @Test
    fun `should create and return message`() {
        val repository = mockk<MessageRepository>()
        val created = Message("Text", "Some ID")
        every { repository.save(any()) } returns created
        val service = MessageService(repository)

        val result = service.save(created)

        assertThat(result).isEqualTo(created)

    }
}