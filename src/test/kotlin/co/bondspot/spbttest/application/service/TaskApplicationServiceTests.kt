package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.application.exception.ApplicationServiceException
import co.bondspot.spbttest.application.exception.ApplicationServiceInternalException
import co.bondspot.spbttest.domain.contract.IAccountRepository
import co.bondspot.spbttest.domain.contract.IFgaProvider
import co.bondspot.spbttest.domain.contract.ITaskRepository
import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.FgaRelTuple
import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.shared.enumeration.HttpStatusCode
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

private class TaskApplicationServiceTests {
    private val id = "some_id"
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
        taskRepository = spyk<ITaskRepository>(recordPrivateCalls = true)
        fga = spyk<IFgaProvider>(recordPrivateCalls = true)
        every { fga.checkRelationship(any()) } returns false
    }

    @Nested
    @DisplayName("when creating a task...")
    inner class CreateTask {
        @Test
        fun `should create and return task`() {
            val task = Task("Text", id = "Some ID")
            val createdTask = task.copy(createdById = reqAccount.id)
            every { taskRepository.create(any()) } returns createdTask
            val service = TaskApplicationService(taskRepository, accountRepository, fga)

            val result = service.create(task, reqAccount = reqAccount)

            assertThat(result).isEqualTo(createdTask)

            verify {
                fga invoke
                    "writeRelationships" withArguments
                    listOf(
                        listOf(
                            FgaRelTuple(
                                "user" to reqAccount.id!!,
                                Task.FgaRelations.OWNER,
                                "task" to createdTask.id!!,
                            )
                        )
                    )
            }
        }
    }

    @Nested
    @DisplayName("when getting a task...")
    inner class GetTask {
        @Test
        fun `throw forbidden if req user is not bonded to task`() {
            val existing = Task("Text", id = id)
            every { taskRepository.create(any()) } returns existing.copy(id = accountId)
            every { fga.checkRelationship(any()) } returns false
            val service = TaskApplicationService(taskRepository, accountRepository, fga)

            every { taskRepository.getById(id) } returns existing.copy(createdById = "not_you")

            val ex = assertThrows<ApplicationServiceException> { service.getById(id, reqAccount) }
            assertThat(ex.message)
                .isEqualTo("Requested resource (Task: '$id') is not bonded to requester")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.FORBIDDEN)
        }

        @Test
        fun `return task bonded to requester`() {
            val existing = Task("Text", id = id)
            val createdTask = existing.copy(createdById = reqAccount.id)
            every { taskRepository.create(any()) } returns createdTask
            every { fga.checkRelationship(any()) } returns true
            val service = TaskApplicationService(taskRepository, accountRepository, fga)

            every { taskRepository.getById(id) } returns createdTask

            val task = service.getById(id, reqAccount)
            assertThat(task).isEqualTo(createdTask)
        }
    }

    @Nested
    @DisplayName("when updating status...")
    inner class UpdateStatus {
        @Test
        fun `throw forbidden if req user is not bonded to task`() {
            val existing = Task("Text", id = id)
            every { taskRepository.create(any()) } returns existing.copy(id = accountId)
            every { fga.checkRelationship(any()) } returns false
            val service = TaskApplicationService(taskRepository, accountRepository, fga)

            every { taskRepository.getById(id) } returns existing.copy(createdById = "not_you")
            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        "user" to reqAccount.id!!,
                        Task.FgaRelations.VIEWER,
                        "task" to id,
                    )
                )
            } returns true

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        "user" to reqAccount.id!!,
                        Task.FgaRelations.CAN_EDIT_DETAILS,
                        "task" to id,
                    )
                )
            } returns false

            val ex =
                assertThrows<ApplicationServiceException> {
                    service.updateStatus(id, Task.Status.IN_PROGRESS, reqAccount)
                }
            assertThat(ex.message)
                .isEqualTo("Requester does not have sufficient permission to perform this action")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.FORBIDDEN)
        }

        @Test
        fun `should update successfully`() {
            val existing = Task("Text", id = id, createdById = reqAccount.id)
            every { taskRepository.create(any()) } returns existing
            every { fga.checkRelationship(any()) } returns true
            val service = TaskApplicationService(taskRepository, accountRepository, fga)

            every { taskRepository.getById(id) } returns existing
            every { fga.checkRelationship(any()) } returns true

            val result = service.updateStatus(id, Task.Status.IN_PROGRESS, reqAccount)

            Assertions.assertThat(result).isTrue()

            verify {
                taskRepository invoke
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
            val existing = Task("Text", id = id)
            every { taskRepository.create(any()) } returns existing.copy(id = accountId)
            every { fga.checkRelationship(any()) } returns false
            val service = TaskApplicationService(taskRepository, accountRepository, fga)

            every { taskRepository.getById(id) } returns existing.copy(createdById = "not_you")

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        "user" to reqAccount.id!!,
                        Task.FgaRelations.VIEWER,
                        "task" to id,
                    )
                )
            } returns true

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        "user" to reqAccount.id!!,
                        Task.FgaRelations.CAN_EDIT_DETAILS,
                        "task" to id,
                    )
                )
            } returns false

            val ex =
                assertThrows<ApplicationServiceException> {
                    service.updateDetails(id, "Editado", reqAccount)
                }
            assertThat(ex.message)
                .isEqualTo("Requester does not have sufficient permission to perform this action")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.FORBIDDEN)
        }

        @Test
        fun `should update successfully`() {
            val existing = Task("Text", id = id, createdById = reqAccount.id)
            every { taskRepository.create(any()) } returns existing
            every { fga.checkRelationship(any()) } returns true
            val service = TaskApplicationService(taskRepository, accountRepository, fga)

            every { taskRepository.getById(id) } returns existing

            val result = service.updateDetails(id, "Editado", reqAccount)

            Assertions.assertThat(result).isTrue()

            verify {
                taskRepository invoke
                    "update" withArguments
                    listOf(id, existing.copy(title = "Editado"))
            }
        }
    }

    @Nested
    @DisplayName("when sharing task with another account...")
    inner class ShareTaskForView() {
        @Test
        fun `throw 500 if not supported relation is passed`() {

            val service = TaskApplicationService(taskRepository, accountRepository, fga)

            val ex =
                assertThrows<ApplicationServiceInternalException> {
                    service.shareWith("someid", accountId2, "admin", reqAccount)
                }

            assertThat(ex.message)
                .isEqualTo("This should NOT be happening at all: Unsupported relation for sharing")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.INTERNAL_SERVER_ERROR)
        }

        @Test
        fun `throw not found if task does not exist`() {
            every { taskRepository.getById(id) } returns null
            val service = TaskApplicationService(taskRepository, accountRepository, fga)
            val ex =
                assertThrows<ApplicationServiceException> {
                    service.shareWith(id, accountId2, reqAccount = reqAccount)
                }
            assertThat(ex.message).isEqualTo("Task not found")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.NOT_FOUND)
        }

        @Test
        fun `throw forbidden if req user is not bonded to task as admin`() {
            val existing = Task("Text", id = id, createdById = "some_id_nada_a_ver")
            every { taskRepository.create(any()) } returns existing
            every { taskRepository.getById(id) } returns existing
            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        "user" to reqAccount.id!!,
                        Task.FgaRelations.VIEWER,
                        "task" to id,
                    )
                )
            } returns true
            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        "user" to reqAccount.id!!,
                        Task.FgaRelations.OWNER,
                        "task" to id,
                    )
                )
            } returns false
            val service = TaskApplicationService(taskRepository, accountRepository, fga)
            val ex =
                assertThrows<ApplicationServiceException> {
                    service.shareWith(id, accountId2, reqAccount = reqAccount)
                }

            assertThat(ex.message).isEqualTo("Requester missing owner relation to task")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.FORBIDDEN)
        }

        @Test
        fun `throw not found if account to share with does not exist`() {
            val existing = Task("Text", id = id, createdById = accountId)
            every { taskRepository.create(any()) } returns existing
            every { taskRepository.getById(id) } returns existing
            every { accountRepository.getById(accountId2) } returns null
            every { fga.checkRelationship(any()) } returns true
            val service = TaskApplicationService(taskRepository, accountRepository, fga)
            val ex =
                assertThrows<ApplicationServiceException> {
                    service.shareWith(id, accountId2, reqAccount = reqAccount)
                }

            assertThat(ex.message).isEqualTo("Account to share with not found")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.NOT_FOUND)
        }

        @Test
        fun `share viewer permission to an account`() {
            val existing = Task("Text", id = id, createdById = reqAccount.id)
            every { taskRepository.create(any()) } returns existing
            every { taskRepository.getById(id) } returns existing
            every { accountRepository.getById(accountId2) } returns account2
            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        "user" to reqAccount.id!!,
                        Task.FgaRelations.VIEWER,
                        "task" to id,
                    )
                )
            } returns true
            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        "user" to reqAccount.id!!,
                        Task.FgaRelations.OWNER,
                        "task" to id,
                    )
                )
            } returns true
            val service = TaskApplicationService(taskRepository, accountRepository, fga)
            val result = service.shareWith(id, accountId2, Task.FgaRelations.VIEWER, reqAccount)

            Assertions.assertThat(result).isTrue()
            verify {
                fga invoke
                    "writeRelationships" withArguments
                    listOf(
                        listOf(
                            FgaRelTuple(
                                "user" to account2.id!!,
                                Task.FgaRelations.VIEWER,
                                "task" to id,
                            )
                        )
                    )
            }
        }
    }

    @Test
    fun `share editor permission to an account`() {
        val existing = Task("Text", id = id, createdById = reqAccount.id)
        every { taskRepository.create(any()) } returns existing
        every { taskRepository.getById(id) } returns existing
        every { accountRepository.getById(accountId2) } returns account2
        every {
            fga.checkRelationship(
                FgaRelTuple(
                    "user" to reqAccount.id!!,
                    Task.FgaRelations.VIEWER,
                    "task" to id,
                )
            )
        } returns true
        every {
            fga.checkRelationship(
                FgaRelTuple("user" to reqAccount.id!!, Task.FgaRelations.OWNER, "task" to id)
            )
        } returns true
        val service = TaskApplicationService(taskRepository, accountRepository, fga)
        val result = service.shareWith(id, accountId2, Task.FgaRelations.EDITOR, reqAccount)

        Assertions.assertThat(result).isTrue()
        verify {
            fga invoke
                "writeRelationships" withArguments
                listOf(
                    listOf(
                        FgaRelTuple(
                            "user" to account2.id!!,
                            Task.FgaRelations.EDITOR,
                            "task" to id,
                        )
                    )
                )
        }
    }
}
