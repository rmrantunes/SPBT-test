package co.bondspot.spbttest.infrastructure.fga

import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.FgaRelTuple
import co.bondspot.spbttest.domain.entity.Task
import dev.openfga.sdk.errors.FgaError
import io.mockk.spyk
import io.mockk.verify
import java.util.*
import kotlin.test.Test
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
}
