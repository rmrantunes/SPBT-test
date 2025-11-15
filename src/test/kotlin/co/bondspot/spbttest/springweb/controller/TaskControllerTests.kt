package co.bondspot.spbttest.springweb.controller

import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.springweb.dto.CreateTaskReqDto
import co.bondspot.spbttest.springweb.service.TaskService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.Ignore

@WebMvcTest(TaskController::class)
@DisplayName("task controller")
class TaskControllerTests() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var taskService: TaskService

    @Nested
    @DisplayName("when creating a task...")
    inner class CreateTask() {

        @Test
        fun `return it if input is valid`() {
            val createdTask = Task("Uma tarefa qualquer", id = "some_id")
            every { taskService.create(any()) } returns createdTask

            mockMvc.perform(
                post("/task")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"title": "Uma tarefa qualquer"}""")
            )
                .andDo { print() }
                .andExpect(status().isCreated)
                .andExpect(header().string("Location", "/task/${createdTask.id}"))
                .andExpect(jsonPath("$.id").value(createdTask.id))
                .andExpect(jsonPath("$.title").value(createdTask.title))
        }

        @Test
        fun `validate input if fields are missing`() {
            mockMvc.perform(
                post("/task")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{}""")
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0]").value("'title' must be a string"))
        }

        @Test
        fun `validate input if fields have type mismatch`() {
            mockMvc.perform(
                post("/task")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{ "description": false, "title": false }""")
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0]").value("'description' must be a string or null"))
                .andExpect(jsonPath("$.errors[1]").value("'title' must be a string"))
        }
    }
}