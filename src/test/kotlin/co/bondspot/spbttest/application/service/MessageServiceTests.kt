package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.entity.Message
import co.bondspot.spbttest.infrastructure.entity.MessageEntity
import co.bondspot.spbttest.infrastructure.repository.MessageRepository
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
        every { repository.save(any()) } returns MessageEntity.fromDomainEntity(created)
        val service = MessageApplicationService(repository)

        val result = service.save(created)

        assertThat(result).isEqualTo(created)

    }

    @Test
    fun `should return message by id`() {
        val repository = mockk<MessageRepository>()
        val message = Message("Text", "Some ID")
        every { repository.findByIdOrNull(message.id!!) } returns MessageEntity.fromDomainEntity(message)
        val service = MessageApplicationService(repository)

        val result = service.findById(message.id!!)

        assertThat(result).isEqualTo(message)

    }

    @Test
    fun `should list messages`() {
        val repository = mockk<MessageRepository>()
        val list = listOf(Message("Text", "Some ID"), Message("Text2", "Some ID2"))
        every { repository.findAll() } returns list.map { MessageEntity.fromDomainEntity(it) }
        val service = MessageApplicationService(repository)

        val result = service.find()

        assertThat(result).isEqualTo(list)
    }
}