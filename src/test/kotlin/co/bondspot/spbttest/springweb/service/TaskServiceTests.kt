package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.application.exception.ApplicationServiceException
import co.bondspot.spbttest.domain.contract.IAccountRepository
import co.bondspot.spbttest.domain.contract.IFgaProvider
import co.bondspot.spbttest.domain.contract.ITaskRepository
import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.shared.enumeration.HttpStatusCode
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

private class TaskServiceTests {
    private val accountId = "accountId"
    private val accountId2 = "accountId2"
    private val reqAccount = Account("some_account", "some_email@example.com", id = accountId)
    private val account2 = Account("some_account_2", "some_email_2@example.com", id = accountId2)

    private lateinit var accountRepository: IAccountRepository
    private lateinit var taskRepository: ITaskRepository
    private lateinit var fga: IFgaProvider

    @BeforeEach
    fun setUp() {
        accountRepository = mockk<IAccountRepository>()
        taskRepository = mockk<ITaskRepository>()
        fga = spyk<IFgaProvider>(recordPrivateCalls = true)
    }

    @Nested
    @DisplayName("when creating a task...")
    inner class CreateTask {
        @Test
        fun `should create and return task`() {
            val repository = mockk<ITaskRepository>()
            val task = Task("Text", id = "Some ID")
            val createdTask = task.copy(createdById = task.id)
            every { repository.create(any()) } returns createdTask
            val service = TaskService(repository, accountRepository, fga)

            val result = service.create(task, reqAccount = reqAccount)

            assertThat(result).isEqualTo(createdTask)
        }
    }

    @Nested
    @DisplayName("when getting a task...")
    inner class GetTask {
        @Test
        fun `throw forbidden if req user is not bonded to task`() {
            val repository = spyk<ITaskRepository>(recordPrivateCalls = true)
            val id = "some_id"
            val existing = Task("Text", id = id)
            every { repository.create(any()) } returns existing.copy(id = accountId)
            val service = TaskService(repository, accountRepository, fga)

            every { repository.getById(id) } returns existing.copy(createdById = "not_you")

            val ex = assertThrows<ApplicationServiceException> { service.getById(id, reqAccount) }
            assertThat(ex.message)
                .isEqualTo("Requested resource (Task: '$id') is not bonded to requester")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.FORBIDDEN)
        }

        @Test
        fun `return task bonded to requester`() {
            val repository = spyk<ITaskRepository>(recordPrivateCalls = true)
            val id = "some_id"
            val existing = Task("Text", id = id)
            val createdTask = existing.copy(createdById = reqAccount.id)
            every { repository.create(any()) } returns createdTask
            val service = TaskService(repository, accountRepository, fga)

            every { repository.getById(id) } returns createdTask

            val task = service.getById(id, reqAccount)
            assertThat(task).isEqualTo(createdTask)
        }
    }

    @Nested
    @DisplayName("when updating status...")
    inner class UpdateStatus {
        @Test
        fun `throw forbidden if req user is not bonded to task`() {
            val repository = spyk<ITaskRepository>(recordPrivateCalls = true)
            val id = "some_id"
            val existing = Task("Text", id = id)
            every { repository.create(any()) } returns existing.copy(id = accountId)
            val service = TaskService(repository, accountRepository, fga)

            every { repository.getById(id) } returns existing.copy(createdById = "not_you")

            val ex =
                assertThrows<ApplicationServiceException> {
                    service.updateStatus(id, Task.Status.IN_PROGRESS, reqAccount)
                }
            assertThat(ex.message)
                .isEqualTo("Requested resource (Task: '$id') is not bonded to requester")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.FORBIDDEN)
        }

        @Test
        fun `should update successfully`() {
            val repository = spyk<ITaskRepository>(recordPrivateCalls = true)
            val id = "some_id"
            val existing = Task("Text", id = id, createdById = reqAccount.id)
            every { repository.create(any()) } returns existing
            val service = TaskService(repository, accountRepository, fga)

            every { repository.getById(id) } returns existing

            val result = service.updateStatus(id, Task.Status.IN_PROGRESS, reqAccount)

            Assertions.assertThat(result).isTrue()

            verify {
                repository invoke
                    "update" withArguments
                    listOf(id, existing.copy(status = Task.Status.IN_PROGRESS))
            }
        }
    }

    @Nested
    @DisplayName("when updating details...")
    inner class UpdateDetails {
        @Test
        fun `throw forbidden if req user is not bonded to task`() {
            val repository = spyk<ITaskRepository>(recordPrivateCalls = true)
            val id = "some_id"
            val existing = Task("Text", id = id)
            every { repository.create(any()) } returns existing.copy(id = accountId)
            val service = TaskService(repository, accountRepository, fga)

            every { repository.getById(id) } returns existing.copy(createdById = "not_you")

            val ex =
                assertThrows<ApplicationServiceException> {
                    service.updateDetails(id, "Editado", reqAccount)
                }
            assertThat(ex.message)
                .isEqualTo("Requested resource (Task: '$id') is not bonded to requester")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.FORBIDDEN)
        }

        @Test
        fun `should update successfully`() {
            val repository = spyk<ITaskRepository>(recordPrivateCalls = true)
            val id = "some_id"
            val existing = Task("Text", id = id, createdById = reqAccount.id)
            every { repository.create(any()) } returns existing
            val service = TaskService(repository, accountRepository, fga)

            every { repository.getById(id) } returns existing

            val result = service.updateDetails(id, "Editado", reqAccount)

            Assertions.assertThat(result).isTrue()

            verify {
                repository invoke
                    "update" withArguments
                    listOf(id, existing.copy(title = "Editado"))
            }
        }
    }

    @Nested
    @DisplayName("when sharing task to another account...")
    inner class ShareTaskForView() {
        @Test
        fun `throw 500 if not supported relation is passed`() {

            val service = TaskService(taskRepository, accountRepository, fga)

            val ex =
                assertThrows<ApplicationServiceException> {
                    service.shareWith("someid", accountId2, "admin", reqAccount)
                }

            assertThat(ex.message).isEqualTo("Internal message: Unsupported relation for sharing")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.INTERNAL_SERVER_ERROR)
        }

        @Test
        fun `throw not found if task does not exist`() {
            val id = "some_id"
            every { taskRepository.getById(id) } returns null
            val service = TaskService(taskRepository, accountRepository, fga)
            val ex =
                assertThrows<ApplicationServiceException> {
                    service.shareWith(id, accountId2, reqAccount = reqAccount)
                }
            assertThat(ex.message).isEqualTo("Task not found")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.NOT_FOUND)
        }

        @Test
        fun `throw forbidden if req user is not bonded to task as admin`() {
            val id = "some_id"
            val existing = Task("Text", id = id, createdById = "some_id_nada_a_ver")
            every { taskRepository.create(any()) } returns existing
            every { taskRepository.getById(id) } returns existing

            val service = TaskService(taskRepository, accountRepository, fga)
            val ex =
                assertThrows<ApplicationServiceException> {
                    service.shareWith(id, accountId2, reqAccount = reqAccount)
                }

            assertThat(ex.message)
                .isEqualTo("Requested resource (Task: '$id') is not bonded to requester")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.FORBIDDEN)
        }

        @Test
        fun `throw not found if account to share with does not exist`() {
            val id = "some_id"
            val existing = Task("Text", id = id, createdById = accountId)
            every { taskRepository.create(any()) } returns existing
            every { taskRepository.getById(id) } returns existing
            every { accountRepository.getById(accountId2) } returns null

            val service = TaskService(taskRepository, accountRepository, fga)
            val ex =
                assertThrows<ApplicationServiceException> {
                    service.shareWith(id, accountId2, reqAccount = reqAccount)
                }

            assertThat(ex.message).isEqualTo("Account to share with not found")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.NOT_FOUND)
        }

        @Test
        fun `enable task for view to another user`() {
            val id = "some_id"
            val existing = Task("Text", id = id, createdById = reqAccount.id)
            every { taskRepository.create(any()) } returns existing
            every { taskRepository.getById(id) } returns existing
            every { accountRepository.getById(accountId2) } returns account2

            val service = TaskService(taskRepository, accountRepository, fga)
            val result = service.shareWith(id, accountId2, "viewer", reqAccount)

            Assertions.assertThat(result).isTrue()
            verify {
                fga invoke
                    "writeRelationship" withArguments
                    listOf("user" to account2.id, "viewer", "task" to id)
            }
        }
    }
}
