package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.application.exception.ApplicationServiceException
import co.bondspot.spbttest.application.exception.ApplicationServiceInternalException
import co.bondspot.spbttest.domain.contract.IAccountRepository
import co.bondspot.spbttest.domain.contract.IFgaProvider
import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import co.bondspot.spbttest.domain.contract.ITaskRepository
import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.FgaRelTuple
import co.bondspot.spbttest.domain.entity.FtsSearchResponse
import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.shared.enumeration.HttpStatusCode
import co.bondspot.spbttest.testutils.KSelect
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import java.util.*
import kotlin.random.Random
import kotlin.test.Test
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.instancio.Instancio
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows

class TaskApplicationServiceTests {
    private val id = "some_id"
    private val accountId = "accountId"
    private val accountId2 = "accountId2"
    private val reqAccount = Account("some_account", "some_email@example.com", id = accountId)
    private val account2 = Account("some_account_2", "some_email_2@example.com", id = accountId2)

    private lateinit var accountRepo: IAccountRepository
    private lateinit var taskRepo: ITaskRepository
    private lateinit var fts: IFullTextSearchProvider
    private lateinit var fga: IFgaProvider

    private fun generateTask() =
        Instancio.of(Task::class.java)
            .set(KSelect.field(Task::id), UUID.randomUUID().toString())
            .set(KSelect.field(Task::createdById), listOf(accountId, accountId2).random())
            .set(KSelect.field(Task::status), Task.Status.entries.random())
            .generate(KSelect.field(Task::title)) { it.text().word().adjective().noun() }
            .generate(KSelect.field(Task::description)) { it.text().word().adjective().noun() }
            .set(KSelect.field(Task::createdById), UUID.randomUUID().toString())
            .set(KSelect.field(Task::lastUpdatedById), UUID.randomUUID().toString())
            .generate(KSelect.field(Task::lastUpdatedAt)) { it.temporal().localDateTime() }
            .generate(KSelect.field(Task::createdAt)) { it.temporal().localDateTime() }
            .create()

    @BeforeEach
    fun setUp() {
        accountRepo = mockk<IAccountRepository>()
        taskRepo = spyk<ITaskRepository>(recordPrivateCalls = true)
        fga = spyk<IFgaProvider>(recordPrivateCalls = true)
        fts = spyk<IFullTextSearchProvider>(recordPrivateCalls = true)
        every { fga.checkRelationship(any()) } returns false
    }

    @Nested
    @DisplayName("when creating a task...")
    inner class CreateTask {
        @Test
        fun `should create and return task`() {
            val task = Task("Text", id = "Some ID")
            val createdTask = task.copy(createdById = reqAccount.id)
            every { taskRepo.create(any()) } returns createdTask
            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)

            val result = service.create(task, reqAccount = reqAccount)

            assertThat(result).isEqualTo(createdTask)

            verify(exactly = 1) {
                fga invoke
                    "writeRelationship" withArguments
                    listOf(
                        FgaRelTuple(
                            Account.ENTITY_NAME to reqAccount.id!!,
                            Task.FgaRelations.OWNER,
                            Task.ENTITY_NAME to createdTask.id!!,
                        )
                    )
            }

            verify(exactly = 1) {
                fts invoke "index" withArguments listOf(Task.ENTITY_NAME, listOf(createdTask))
            }
        }
    }

    @Nested
    @DisplayName("when getting a task...")
    inner class GetTask {
        @Test
        fun `throw forbidden if req user is not bonded to task`() {
            val existing = Task("Text", id = id)
            every { taskRepo.create(any()) } returns existing.copy(id = accountId)
            every { fga.checkRelationship(any()) } returns false
            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)

            every { taskRepo.getById(id) } returns existing.copy(createdById = "not_you")

            val ex = assertThrows<ApplicationServiceException> { service.getById(id, reqAccount) }
            assertThat(ex.message).isEqualTo("Requested resource is not bonded to requester")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.FORBIDDEN)
        }

        @Test
        fun `return task bonded to requester`() {
            val existing = Task("Text", id = id)
            val createdTask = existing.copy(createdById = reqAccount.id)
            every { taskRepo.create(any()) } returns createdTask
            every { fga.checkRelationship(any()) } returns true
            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)

            every { taskRepo.getById(id) } returns createdTask

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
            every { taskRepo.create(any()) } returns existing.copy(id = accountId)
            every { fga.checkRelationship(any()) } returns false
            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)

            every { taskRepo.getById(id) } returns existing.copy(createdById = "not_you")
            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.VIEWER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns true

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.CAN_EDIT_DETAILS,
                        Task.ENTITY_NAME to id,
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
            every { taskRepo.create(any()) } returns existing
            every { fga.checkRelationship(any()) } returns true
            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)

            every { taskRepo.getById(id) } returns existing
            every { fga.checkRelationship(any()) } returns true

            val result = service.updateStatus(id, Task.Status.IN_PROGRESS, reqAccount)

            Assertions.assertThat(result).isTrue()

            val updated = existing.copy(status = Task.Status.IN_PROGRESS)

            verify { taskRepo invoke "update" withArguments listOf(id, updated) }

            verify(exactly = 1) {
                fts invoke "index" withArguments listOf(Task.ENTITY_NAME, listOf(updated))
            }
        }
    }

    @Nested
    @DisplayName("when updating details...")
    inner class UpdateDetails {
        @Test
        fun `throw forbidden if req user is not bonded to task`() {
            val existing = Task("Text", id = id)
            every { taskRepo.create(any()) } returns existing.copy(id = accountId)
            every { fga.checkRelationship(any()) } returns false
            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)

            every { taskRepo.getById(id) } returns existing.copy(createdById = "not_you")

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.VIEWER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns true

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.CAN_EDIT_DETAILS,
                        Task.ENTITY_NAME to id,
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
            every { taskRepo.create(any()) } returns existing
            every { fga.checkRelationship(any()) } returns true
            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)

            every { taskRepo.getById(id) } returns existing

            val result = service.updateDetails(id, "Editado", reqAccount)

            Assertions.assertThat(result).isTrue()

            val updated = existing.copy(title = "Editado")
            verify { taskRepo invoke "update" withArguments listOf(id, updated) }

            verify(exactly = 1) {
                fts invoke "index" withArguments listOf(Task.ENTITY_NAME, listOf(updated))
            }
        }
    }

    @Nested
    @DisplayName("when sharing task with another account...")
    inner class ShareTaskForView {
        @Test
        fun `throw 500 if not supported relation is passed`() {

            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)

            val ex =
                assertThrows<ApplicationServiceInternalException> {
                    service.shareWith("someid", accountId2, "admin", reqAccount)
                }

            assertThat(ex.message)
                .isEqualTo(
                    "${ApplicationServiceInternalException.MESSAGE_HEAD} Unsupported relation for sharing"
                )
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.INTERNAL_SERVER_ERROR)
        }

        @Test
        fun `throw not found if task does not exist`() {
            every { taskRepo.getById(id) } returns null
            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)
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
            every { taskRepo.create(any()) } returns existing
            every { taskRepo.getById(id) } returns existing
            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.VIEWER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns true
            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.OWNER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns false
            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)
            val ex =
                assertThrows<ApplicationServiceException> {
                    service.shareWith(id, accountId2, reqAccount = reqAccount)
                }

            assertThat(ex.message)
                .isEqualTo("Requester does not have sufficient permission to perform this action")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.FORBIDDEN)
        }

        @Test
        fun `throw not found if account to share with does not exist`() {
            val existing = Task("Text", id = id, createdById = accountId)
            every { taskRepo.create(any()) } returns existing
            every { taskRepo.getById(id) } returns existing
            every { accountRepo.getById(accountId2) } returns null
            every { fga.checkRelationship(any()) } returns true
            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)
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
            every { taskRepo.create(any()) } returns existing
            every { taskRepo.getById(id) } returns existing
            every { accountRepo.getById(accountId2) } returns account2
            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.VIEWER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns true
            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.OWNER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns true
            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)
            val result = service.shareWith(id, accountId2, Task.FgaRelations.VIEWER, reqAccount)

            Assertions.assertThat(result).isTrue()
            verify {
                fga invoke
                    "writeRelationship" withArguments
                    listOf(
                        FgaRelTuple(
                            Account.ENTITY_NAME to account2.id!!,
                            Task.FgaRelations.VIEWER,
                            Task.ENTITY_NAME to id,
                        )
                    )
            }
        }
    }

    @Test
    fun `share editor permission to an account`() {
        val existing = Task("Text", id = id, createdById = reqAccount.id)
        every { taskRepo.create(any()) } returns existing
        every { taskRepo.getById(id) } returns existing
        every { accountRepo.getById(accountId2) } returns account2
        every {
            fga.checkRelationship(
                FgaRelTuple(
                    Account.ENTITY_NAME to reqAccount.id!!,
                    Task.FgaRelations.VIEWER,
                    Task.ENTITY_NAME to id,
                )
            )
        } returns true
        every {
            fga.checkRelationship(
                FgaRelTuple(
                    Account.ENTITY_NAME to reqAccount.id!!,
                    Task.FgaRelations.OWNER,
                    Task.ENTITY_NAME to id,
                )
            )
        } returns true
        val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)
        val result = service.shareWith(id, accountId2, Task.FgaRelations.WRITER, reqAccount)

        Assertions.assertThat(result).isTrue()
        verify {
            fga invoke
                "writeRelationship" withArguments
                listOf(
                    FgaRelTuple(
                        Account.ENTITY_NAME to account2.id!!,
                        Task.FgaRelations.WRITER,
                        Task.ENTITY_NAME to id,
                    )
                )
        }
    }

    @Nested
    @DisplayName("when listing tasks...")
    inner class ListTasks {
        @Test
        fun `should return an empty list if NONE is related to requester`() {
            every {
                fga.listObjects(
                    Account.ENTITY_NAME to reqAccount.id!!,
                    Task.FgaRelations.VIEWER,
                    Task.ENTITY_NAME,
                )
            } returns emptyList()

            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)
            val result = service.list(reqAccount = reqAccount)
            Assertions.assertThat(result).isEmpty()
        }

        @Test
        fun `should return a list of tasks related to requester according to query term`() {
            val tasks = buildList { repeat(30) { add(generateTask()) } }
            val term = "termo_qualquer"

            val relatedTasks =
                tasks.filter { it.createdById == reqAccount.id || Random.nextBoolean() }
            val relatedObjects = relatedTasks.map { "task" to it.id!! }

            // List FGA objects related to user.
            // This approach is valid for small applications (~1000 objects returned)
            // To handle more, see https://openfga.dev/docs/interacting/search-with-permissions
            every {
                fga.listObjects(
                    Account.ENTITY_NAME to reqAccount.id!!,
                    Task.FgaRelations.VIEWER,
                    Task.ENTITY_NAME,
                )
            } returns relatedObjects

            // When considering using fga.listObjects() approach:
            // In cases of full-text search, grab the intersection between user-related objects and
            // the full-text search provider response.
            // Or full-text search passing the user-related objects ids.

            val tasksFromFts = tasks.subList(0, 5)

            every {
                fts.search(
                    indexUid = Task.ENTITY_NAME,
                    query = term,
                    ids = relatedTasks.map { it.id!! },
                )
            } returns
                FtsSearchResponse(buildList { addAll(tasksFromFts.map { mapOf("id" to it.id!!) }) })

            // query by retrieved ids the tasks
            every { taskRepo.listByIds(tasksFromFts.map { it.id!! }) } returns tasksFromFts

            // return the tasks

            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)
            val result = service.list(queryTerm = term, reqAccount = reqAccount)
            Assertions.assertThat(result).isEqualTo(tasksFromFts)
        }
    }

    @Nested
    @DisplayName("when listing accounts related to task...")
    inner class ListAccountsRelated {
        // for a given id bring all users related as viewer

        @Test
        fun `should throw an requester is not owner`() {
            val existing = Task("Text", id = id, createdById = account2.id)
            every { taskRepo.create(any()) } returns existing
            every { taskRepo.getById(any()) } returns existing

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.VIEWER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns true

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.OWNER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns false

            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)
            val ex =
                assertThrows<ApplicationServiceException> {
                    service.listRelatedAccounts(id, reqAccount = reqAccount)
                }

            assertThat(ex.message)
                .isEqualTo("Requester does not have sufficient permission to perform this action")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.FORBIDDEN)
        }

        @Test
        fun `should return a list of accounts containing at least the owner`() {
            val existing = Task("Text", id = id, createdById = reqAccount.id)
            every { taskRepo.create(any()) } returns existing
            every { taskRepo.getById(any()) } returns existing

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.VIEWER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns true

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.OWNER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns true

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.OWNER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns true

            every { fga.listRelatedUsers(Task.ENTITY_NAME to id, Task.FgaRelations.VIEWER) } returns
                listOf(Account.ENTITY_NAME to reqAccount.id!!)

            every { accountRepo.listByIds(listOf(reqAccount.id!!)) } returns listOf(reqAccount)

            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)
            val result = service.listRelatedAccounts(id, reqAccount = reqAccount)
            assertThat(result).isEqualTo(listOf(reqAccount))
        }

        @Test
        fun `should return list of accounts related to task via share`() {
            val existing = Task("Text", id = id, createdById = reqAccount.id)
            every { taskRepo.create(any()) } returns existing
            every { taskRepo.getById(any()) } returns existing

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.VIEWER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns true

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.OWNER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns true

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.OWNER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns true

            val fgaRelatedUsers =
                listOf(Account.ENTITY_NAME to reqAccount.id!!, Account.ENTITY_NAME to account2.id!!)

            every { fga.listRelatedUsers(Task.ENTITY_NAME to id, Task.FgaRelations.VIEWER) } returns
                fgaRelatedUsers

            every { accountRepo.listByIds(fgaRelatedUsers.map { it.second }) } returns
                listOf(reqAccount, account2)

            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)
            val result = service.listRelatedAccounts(id, reqAccount = reqAccount)
            assertThat(result).isEqualTo(listOf(reqAccount, account2))
        }
    }

    @Nested
    @DisplayName("when unsharing a task with an account...")
    inner class Unsharing {
        @Test
        fun `forbidden if requester is not the owner`() {
            val existing = Task("Text", id = id, createdById = accountId2)
            every { taskRepo.create(any()) } returns existing
            every { taskRepo.getById(any()) } returns existing

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.VIEWER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns true

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.OWNER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns false

            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)
            val ex =
                assertThrows<ApplicationServiceException> {
                    service.revokeShare(id, UUID.randomUUID().toString(), reqAccount = reqAccount)
                }

            assertThat(ex.message)
                .isEqualTo("Requester does not have sufficient permission to perform this action")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.FORBIDDEN)
        }

        @Test
        fun `forbidden if owner is trying to self revoke`() {
            val existing = Task("Text", id = id, createdById = accountId2)
            every { taskRepo.create(any()) } returns existing
            every { taskRepo.getById(any()) } returns existing

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.VIEWER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns true

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.OWNER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns true

            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)
            val ex =
                assertThrows<ApplicationServiceException> {
                    service.revokeShare(id, reqAccount.id!!, reqAccount = reqAccount)
                }

            assertThat(ex.message).isEqualTo("Owner cannot self revoke")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.FORBIDDEN)
        }

        @Test
        fun `revoke permission from account`() {
            val existing = Task("Text", id = id, createdById = reqAccount.id)
            every { taskRepo.create(any()) } returns existing
            every { taskRepo.getById(any()) } returns existing

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.VIEWER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns true

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to reqAccount.id!!,
                        Task.FgaRelations.OWNER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns true

            val accountIdToRevokeFrom = UUID.randomUUID().toString()

            every {
                fga.checkRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to accountIdToRevokeFrom,
                        Task.FgaRelations.WRITER,
                        Task.ENTITY_NAME to id,
                    )
                )
            } returns true

            val service = TaskApplicationService(taskRepo, accountRepo, fga, fts)
            val result = service.revokeShare(id, accountIdToRevokeFrom, reqAccount = reqAccount)
            assertThat(result).isTrue()
            verify(exactly = 1) {
                fga invoke
                    "deleteRelationship" withArguments
                    listOf(
                        FgaRelTuple(
                            Account.ENTITY_NAME to accountIdToRevokeFrom,
                            Task.FgaRelations.WRITER,
                            Task.ENTITY_NAME to id,
                        )
                    )
            }
        }
    }
}
