package co.bondspot.spbttest.springweb.controller

import co.bondspot.spbttest.springweb.dto.CreateTaskReqDto
import co.bondspot.spbttest.springweb.service.TaskService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(TaskController::class)
class TaskControllerTests(@param:Autowired private val objectMapper: ObjectMapper) {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var taskService: TaskService

    @Test
    fun `should create and return a task`() {
        val body = CreateTaskReqDto("Uma tarefa qualquer")
        val createdTask = body.toDomainEntity().copy(id = "some_id")
        every { taskService.create(any()) } returns createdTask

        mockMvc.perform(
            post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        )
            .andDo { print() }
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", "/task/${createdTask.id}"))
            .andExpect(jsonPath("$.id").value(createdTask.id))
            .andExpect(jsonPath("$.title").value(createdTask.title))
    }

    @Test
    fun `validate task input`() {
        mockMvc.perform(
            post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "title": "" }""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors[0]").value("'title' must not be blank"))
    }
}