package co.bondspot.spbttest.infrastructure.fga

import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.FgaRelTuple
import co.bondspot.spbttest.domain.entity.Task
import dev.openfga.sdk.errors.FgaError
import io.mockk.spyk
import io.mockk.verify
import java.util.*
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class OpenFgaProviderTests {
    private val accountId = "mimosa"
    private val taskId = UUID.randomUUID().toString()

    private val fga = spyk(OpenFgaProvider())

    @Nested
    @DisplayName("writeRelationships")
    inner class WriteRelationships {
        @Test
        fun `write new relationships at once and throw already exists error`() {
            assertDoesNotThrow {
                fga.writeRelationships(
                    listOf(
                        FgaRelTuple(
                            Account.ENTITY_NAME to accountId,
                            Task.FgaRelations.OWNER,
                            Task.ENTITY_NAME to UUID.randomUUID().toString(),
                        ),
                        FgaRelTuple(
                            Account.ENTITY_NAME to accountId,
                            Task.FgaRelations.OWNER,
                            Task.ENTITY_NAME to taskId,
                        ),
                    )
                )
            }

            val ex =
                assertThrows<OpenFgaProviderException> {
                    fga.writeRelationships(
                        listOf(
                            FgaRelTuple(
                                Account.ENTITY_NAME to accountId,
                                Task.FgaRelations.OWNER,
                                Task.ENTITY_NAME to taskId,
                            )
                        )
                    )
                }

            assertThat(ex.message).startsWith("cannot write a tuple which already exists")
            assertThat(ex.cause).isInstanceOf(FgaError::class.java)
        }

        @Ignore fun `handle unexisted relation`() {}

        @Ignore fun `handle unexisted actor or subject entity type`() {}
    }

    @Nested
    @DisplayName("writeRelationship")
    inner class WriteRelationship {
        @Test
        fun `write a new relationship using writeRelationship()`() {

            assertDoesNotThrow {
                fga.writeRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to accountId,
                        Task.FgaRelations.OWNER,
                        Task.ENTITY_NAME to taskId,
                    )
                )
            }

            verify(exactly = 1) {
                fga invoke
                    "writeRelationships" withArguments
                    listOf(
                        listOf(
                            FgaRelTuple(
                                Account.ENTITY_NAME to accountId,
                                Task.FgaRelations.OWNER,
                                Task.ENTITY_NAME to taskId,
                            )
                        )
                    )
            }
        }
    }

    @Nested
    @DisplayName("deleteRelationships")
    inner class DeleteRelationships {
        @Test
        fun `delete existing relationships`() {
            val taskId1 = UUID.randomUUID().toString()
            val taskId2 = UUID.randomUUID().toString()

            fga.writeRelationships(
                listOf(
                    FgaRelTuple(
                        Account.ENTITY_NAME to accountId,
                        Task.FgaRelations.OWNER,
                        Task.ENTITY_NAME to taskId1,
                    ),
                    FgaRelTuple(
                        Account.ENTITY_NAME to accountId,
                        Task.FgaRelations.OWNER,
                        Task.ENTITY_NAME to taskId2,
                    ),
                )
            )

            assertDoesNotThrow {
                fga.deleteRelationships(
                    listOf(
                        FgaRelTuple(
                            Account.ENTITY_NAME to accountId,
                            Task.FgaRelations.OWNER,
                            Task.ENTITY_NAME to taskId1,
                        ),
                        FgaRelTuple(
                            Account.ENTITY_NAME to accountId,
                            Task.FgaRelations.OWNER,
                            Task.ENTITY_NAME to taskId2,
                        ),
                    )
                )
            }

            val ex =
                assertThrows<OpenFgaProviderException> {
                    fga.deleteRelationships(
                        listOf(
                            FgaRelTuple(
                                Account.ENTITY_NAME to accountId,
                                Task.FgaRelations.OWNER,
                                Task.ENTITY_NAME to taskId1,
                            )
                        )
                    )
                }

            assertThat(ex.message).startsWith("cannot delete a tuple which does not exist")
            assertThat(ex.cause).isInstanceOf(FgaError::class.java)
        }
    }

    @Nested
    @DisplayName("deleteRelationship")
    inner class DeleteRelationship {
        @Test
        fun `delete a relationship using deleteRelationship()`() {
            val taskId1 = UUID.randomUUID().toString()

            fga.writeRelationship(
                FgaRelTuple(
                    Account.ENTITY_NAME to accountId,
                    Task.FgaRelations.OWNER,
                    Task.ENTITY_NAME to taskId1,
                )
            )

            assertDoesNotThrow {
                fga.deleteRelationship(
                    FgaRelTuple(
                        Account.ENTITY_NAME to accountId,
                        Task.FgaRelations.OWNER,
                        Task.ENTITY_NAME to taskId1,
                    )
                )
            }

            verify(exactly = 1) {
                fga invoke
                    "deleteRelationships" withArguments
                    listOf(
                        listOf(
                            FgaRelTuple(
                                Account.ENTITY_NAME to accountId,
                                Task.FgaRelations.OWNER,
                                Task.ENTITY_NAME to taskId1,
                            )
                        )
                    )
            }
        }
    }

    @Nested
    @DisplayName("checkRelationship")
    inner class CheckRelationship {
        @Test
        fun `check relationship using checkRelationship()`() {
            val taskId1 = UUID.randomUUID().toString()
            val accountId2 = UUID.randomUUID().toString()

            val relationship =
                FgaRelTuple(
                    Account.ENTITY_NAME to accountId,
                    Task.FgaRelations.OWNER,
                    Task.ENTITY_NAME to taskId1,
                )

            fga.writeRelationship(relationship)

            assertEquals(true, fga.checkRelationship(relationship))
            assertEquals(
                false,
                fga.checkRelationship(
                    relationship.copy(actor = relationship.actor.first to accountId2)
                ),
            )

            val ex = assertThrows<OpenFgaProviderException> {
                fga.checkRelationship(relationship.copy(relation = "not_a_relation"))
            }

            assertThat(ex.message).startsWith("relation 'task#not_a_relation' not found")
            assertThat(ex.cause).isInstanceOf(FgaError::class.java)
        }
    }
}
