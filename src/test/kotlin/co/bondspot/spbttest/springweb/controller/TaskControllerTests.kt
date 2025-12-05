package co.bondspot.spbttest.springweb.controller

import co.bondspot.spbttest.domain.contract.ITaskRepository
import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.springweb.persistence.TaskRepository
import java.util.*
import kotlin.test.Ignore
import kotlin.test.assertTrue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("task controller")
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskControllerTests {

    @Autowired private lateinit var context: WebApplicationContext

    @Autowired private lateinit var mockMvc: MockMvc

    @Autowired private lateinit var jpaTaskRepository: TaskRepository

    @Autowired private lateinit var taskRepositoryImpl: ITaskRepository

    private val jwtMock =
        Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("sub", UUID.randomUUID().toString())
            .claim("preferred_username", "zezindaesquina")
            .claim("email", "zedaesquina@example.com")
            .build()

    @BeforeEach
    fun beforeEach() {
        mockMvc =
            MockMvcBuilders.webAppContextSetup(context)
                .apply<DefaultMockMvcBuilder>(springSecurity())
                .build()
        jpaTaskRepository.deleteAll()
    }

    @AfterEach
    fun afterEach() {
        jpaTaskRepository.deleteAll()
    }

    @Nested
    @DisplayName("when creating a task...")
    inner class CreateTask() {

        @Test
        fun `return it if input is valid`() {
            mockMvc
                .perform(
                    post("/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"title": "Uma tarefa qualquer"}""")
                        .with(jwt().jwt(jwtMock))
                )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.requested.task.id").isString)
                .andExpect(jsonPath("$.requested.task.title").value("Uma tarefa qualquer"))
                .andExpect(jsonPath("$.requested.task.createdById").value(jwtMock.subject))
                .andReturn()
                .also {
                    assertTrue {
                        it.response.getHeaderValue("Location").toString().startsWith("/task")
                    }
                }
        }

        @Test
        fun `validate input if fields are missing`() {
            mockMvc
                .perform(
                    post("/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{}""")
                        .with(jwt().jwt(jwtMock))
                )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0].message").value("'title' must be a string"))
        }

        @Test
        fun `validate input if fields have type mismatch`() {
            mockMvc
                .perform(
                    post("/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{ "description": false, "title": false }""")
                        .with(jwt().jwt(jwtMock))
                )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0].message").value("'description' must be a string or null"))
                .andExpect(jsonPath("$.errors[1].message").value("'title' must be a string"))
        }
    }

    @Nested
    @DisplayName("when listing a task...")
    inner class ListTasks() {
        @Test
        fun `return list of created tasks`() {
            repeat(3) { taskRepositoryImpl.create(Task("Task $it", createdById = jwtMock.subject)) }

            mockMvc
                .perform(get("/task").with(jwt().jwt(jwtMock)))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.requested.tasks").isArray)
                .andExpect(jsonPath("$.requested.tasks.size()").value(3))
                .also { m ->
                    repeat(3) { m.andExpect(jsonPath("$.requested.tasks[${it}].title").value("Task $it")) }
                }
        }

        @Ignore fun `return a paginated list of tasks`() {}
    }

    @Nested
    @DisplayName("when getting a task...")
    inner class GetTask() {
        @Test
        fun `return 404 if no task found with given id`() {
            mockMvc
                .perform(get("/task/${UUID.randomUUID()}").with(jwt().jwt(jwtMock)))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.errors[0].message").value("Task not found"))
                .andExpect(jsonPath("$.errors[0].type").value("NOT_FOUND"))
        }

        @Test
        fun `return task found with given id`() {
            val created =
                taskRepositoryImpl.create(
                    Task("Task 1", description = "alguma coisa aí", createdById = jwtMock.subject)
                )

            mockMvc
                .perform(get("/task/${created.id}").with(jwt().jwt(jwtMock)))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.requested.task.id").value(created.id))
                .andExpect(jsonPath("$.requested.task.title").value(created.title))
                .andExpect(jsonPath("$.requested.task.description").value(created.description))
                .andExpect(jsonPath("$.requested.task.status").value("PENDING"))
        }
    }

    @Nested
    @DisplayName("when updating a task details...")
    inner class UpdateTaskDetails {
        @Test
        fun `return 404 if no task found with given id`() {
            mockMvc
                .perform(
                    patch("/task/${UUID.randomUUID()}/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"title":  "Task 1 updated"}""".trimIndent())
                        .with(jwt().jwt(jwtMock))
                )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.errors[0].message").value("Task not found"))
        }

        @Ignore
        fun `validate request body`() {
            TODO("Test request body validation")
        }

        @Test
        fun `return success result for found task updated`() {
            val created =
                taskRepositoryImpl.create(
                    Task("Task 1", description = "alguma coisa aí", createdById = jwtMock.subject)
                )
            val id = created.id!!

            mockMvc
                .perform(
                    patch("/task/$id/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"title":  "Task 1 updated"}""".trimIndent())
                        .with(jwt().jwt(jwtMock))
                )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.requested.updateSuccessful").value(true))

            val updated = taskRepositoryImpl.getById(id)

            assertThat(updated?.title).isEqualTo("Task 1 updated")
        }
    }

    @Nested
    @DisplayName("when updating a task status...")
    inner class UpdateTaskStatus {
        @Test
        fun `validate request body status options`() {
            mockMvc
                .perform(
                    patch("/task/${UUID.randomUUID()}/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"status":  "DOING"}""".trimIndent())
                        .with(jwt().jwt(jwtMock))
                )
                .andExpect(status().isBadRequest)
                // .andExpect(jsonPath("$.errors[0]")..messagevalue("'status' must be a string'"))
                .andExpect(
                    jsonPath("$.errors[0].message")
                        .value("'status' must be one of: ${Task.Status.entries.joinToString(", ")}")
                )
        }

        @Test
        fun `return 404 if no task found with given id`() {
            mockMvc
                .perform(
                    patch("/task/${UUID.randomUUID()}/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"status":  "IN_PROGRESS"}""".trimIndent())
                        .with(jwt().jwt(jwtMock))
                )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.errors[0].message").value("Task not found"))
        }

        @Test
        fun `return success result for found task updated`() {
            val created =
                taskRepositoryImpl.create(
                    Task("Task 1", description = "alguma coisa aí", createdById = jwtMock.subject)
                )
            val id = created.id!!

            for (value in Task.Status.entries) {
                mockMvc
                    .perform(
                        patch("/task/$id/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""{"status":  "$value"}""".trimIndent())
                            .with(jwt().jwt(jwtMock))
                    )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.requested.updateSuccessful").value(true))

                val updated = taskRepositoryImpl.getById(id)

                assertThat(updated?.status).isEqualTo(value)
            }
        }
    }
}
