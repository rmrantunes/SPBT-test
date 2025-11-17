package co.bondspot.spbttest.springweb.controller

import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.domain.signature.TaskRepositorySignature
import co.bondspot.spbttest.springweb.persistence.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*
import kotlin.test.Ignore
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

    @Nested
    @DisplayName("when listing a task...")
    inner class ListTasks() {
        @Test
        fun `return list of created tasks`() {
            repeat(3) {
                taskRepositorySignature.create(Task("Task $it"))
            }

            mockMvc.perform(
                get("/task")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.size()").value(3))
                .also { m ->
                    repeat(3) {
                        m.andExpect(jsonPath("$[${it}].title").value("Task $it"))
                    }
                }
        }

        @Ignore
        fun `return a paginated list of tasks`() {
        }
    }

    @Nested
    @DisplayName("when getting a task...")
    inner class GetTask() {
        @Test
        fun `return 404 if no task found with given id`() {
            mockMvc.perform(
                get("/task/${UUID.randomUUID()}")
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.errors[0]").value("Task not found"))
        }

        @Test
        fun `return task found with given id`() {
            val created = taskRepositorySignature.create(Task("Task 1", description = "alguma coisa aí"))

            mockMvc.perform(
                get("/task/${created.id}")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(created.id))
                .andExpect(jsonPath("$.title").value(created.title))
                .andExpect(jsonPath("$.description").value(created.description))
                .andExpect(jsonPath("$.status").value("PENDING"))
        }
    }

    @Nested
    @DisplayName("when updating a task details...")
    inner class UpdateTaskDetails {
        @Test
        fun `return 404 if no task found with given id`() {
            mockMvc.perform(
                patch("/task/${UUID.randomUUID()}/details")
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.errors[0]").value("Task not found"))
        }

        @Test
        fun `return success result for found task updated`() {
            val created = taskRepositorySignature.create(Task("Task 1", description = "alguma coisa aí"))
            val id = created.id!!

            mockMvc.perform(
                patch("/task/$id/details")
                    .contentType(MediaType.APPLICATION_JSON).content(
                    """{"title":  "Task 1 updated"}""".trimIndent()
                )
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.updated").value(true))

            val updated = taskRepositorySignature.getById(id)

            assertThat(updated?.title).isEqualTo("Task 1 updated")
        }
    }
}