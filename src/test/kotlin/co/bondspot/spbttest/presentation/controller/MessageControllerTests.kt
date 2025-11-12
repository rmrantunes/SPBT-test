package co.bondspot.spbttest.presentation.controller

import co.bondspot.spbttest.application.service.MessageService
import co.bondspot.spbttest.domain.entity.Message
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(MessageController::class)
class MessageControllerTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var messageService: MessageService

    @Test
    fun `should get get message`() {
        val id = "some_uuid"
        val created = Message("My message", id = id)
        every { messageService.findMessageById(id) } returns created

        mockMvc.perform(get("/message/${created.id}"))
            .andDo { print() }
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.text").value(created.text))

        verify { messageService.findMessageById(id) }
    }
}