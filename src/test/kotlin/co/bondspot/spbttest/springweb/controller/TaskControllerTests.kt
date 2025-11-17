package co.bondspot.spbttest.springweb.controller

import co.bondspot.spbttest.domain.signature.TaskRepositorySignature
import co.bondspot.spbttest.springweb.persistence.TaskRepository
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("task controller")
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskControllerTests() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var taskRepository: TaskRepository

    @Autowired
    private lateinit var taskRepositorySignature: TaskRepositorySignature

    @BeforeEach
    fun beforeEach() {
        taskRepository.deleteAll()
    }

    @AfterEach
    fun afterEach() {
        taskRepository.deleteAll()
    }

    @Nested
    @DisplayName("when creating a task...")
    inner class CreateTask() {

        @Test
        fun `return it if input is valid`() {
            mockMvc.perform(
                post("/task")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"title": "Uma tarefa qualquer"}""")
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").isString)
                .andExpect(jsonPath("$.title").value("Uma tarefa qualquer"))
                .andReturn()
                .also {
                    assertTrue { it.response.getHeaderValue("Location").toString().startsWith("/task") }
                }
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