package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.entity.Message
import co.bondspot.spbttest.domain.repository.MessageRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.springframework.data.repository.findByIdOrNull
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

    @Test
    fun `should return message by id`() {
        val repository = mockk<MessageRepository>()
        val message = Message("Text", "Some ID")
        every { repository.findByIdOrNull(message.id!!) } returns message
        val service = MessageService(repository)

        val result = service.findMessageById(message.id!!)

        assertThat(result).isEqualTo(message)

    }

    @Test
    fun `should list messages`() {
        val repository = mockk<MessageRepository>()
        val list = listOf(Message("Text", "Some ID"), Message("Text2", "Some ID2"))
        every { repository.findAll() } returns list
        val service = MessageService(repository)

        val result = service.findMessages()

        assertThat(result).isEqualTo(list)
    }
}