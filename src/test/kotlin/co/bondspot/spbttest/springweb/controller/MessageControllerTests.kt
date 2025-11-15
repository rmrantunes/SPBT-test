package co.bondspot.spbttest.springweb.controller

import co.bondspot.spbttest.domain.entity.Message
import co.bondspot.spbttest.springweb.service.MessageService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(MessageController::class)
class MessageControllerTests() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var messageService: MessageService

    @Test
    fun `should create message`() {
        val id = "some_uuid"
        val body = Message("My message")
        val created = body.copy(id = id)
        every { messageService.save(body) } returns created

        mockMvc.perform(
            post("/message")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "text": "My message" }""")
        )
            .andDo { print() }
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/message/$id"))
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.text").value(created.text))

        verify(exactly = 1) { messageService.save(body) }
    }

    @Test
    fun `should get get message`() {
        val id = "some_uuid"
        val created = Message("My message", id = id)
        every { messageService.findById(id) } returns created

        mockMvc.perform(get("/message/${created.id}"))
            .andDo { print() }
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.text").value(created.text))

        verify { messageService.findById(id) }
    }
}