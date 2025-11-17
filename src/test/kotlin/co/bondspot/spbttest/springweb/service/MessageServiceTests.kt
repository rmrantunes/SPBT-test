package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.domain.contract.MessageRepositoryContract
import co.bondspot.spbttest.domain.entity.Message
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import kotlin.test.Test

class MessageServiceTests {

    @Test
    fun `should create and return message`() {
        val repository = mockk<MessageRepositoryContract>()
        val created = Message("Text", "Some ID")
        every { repository.save(any()) } returns created
        val service = MessageService(repository)

        val result = service.save(created)

        Assertions.assertThat(result).isEqualTo(created)

    }

    @Test
    fun `should return message by id`() {
        val repository = mockk<MessageRepositoryContract>()
        val message = Message("Text", "Some ID")
        every { repository.findById(message.id!!) } returns message
        val service = MessageService(repository)

        val result = service.findById(message.id!!)

        Assertions.assertThat(result).isEqualTo(message)

    }

    @Test
    fun `should list messages`() {
        val repository = mockk<MessageRepositoryContract>()
        val list = listOf(Message("Text", "Some ID"), Message("Text2", "Some ID2"))
        every { repository.find() } returns list
        val service = MessageService(repository)

        val result = service.find()

        Assertions.assertThat(result).isEqualTo(list)
    }
}