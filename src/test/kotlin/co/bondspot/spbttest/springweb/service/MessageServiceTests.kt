package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.springweb.persistence.MessageEntity
import co.bondspot.spbttest.springweb.persistence.MessageRepository
import co.bondspot.spbttest.domain.entity.Message
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.springframework.data.repository.findByIdOrNull
import kotlin.test.Test

class MessageServiceTests {

    @Test
    fun `should create and return message`() {
        val repository = mockk<MessageRepository>()
        val created = Message("Text", "Some ID")
        every { repository.save(any()) } returns MessageEntity.fromDomain(created)
        val service = MessageService(repository)

        val result = service.save(created)

        Assertions.assertThat(result).isEqualTo(created)

    }

    @Test
    fun `should return message by id`() {
        val repository = mockk<MessageRepository>()
        val message = Message("Text", "Some ID")
        every { repository.findByIdOrNull(message.id!!) } returns MessageEntity.fromDomain(message)
        val service = MessageService(repository)

        val result = service.findById(message.id!!)

        Assertions.assertThat(result).isEqualTo(message)

    }

    @Test
    fun `should list messages`() {
        val repository = mockk<MessageRepository>()
        val list = listOf(Message("Text", "Some ID"), Message("Text2", "Some ID2"))
        every { repository.findAll() } returns list.map { MessageEntity.fromDomain(it) }
        val service = MessageService(repository)

        val result = service.find()

        Assertions.assertThat(result).isEqualTo(list)
    }
}