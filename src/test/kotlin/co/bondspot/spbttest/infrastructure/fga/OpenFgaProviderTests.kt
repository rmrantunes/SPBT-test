package co.bondspot.spbttest.infrastructure.fga

import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.FgaRelTuple
import co.bondspot.spbttest.domain.entity.Task
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.*
import kotlin.test.Test

class OpenFgaProviderTests {
    private val accountId = "mimosa"
    private val taskId = UUID.randomUUID().toString()

    private val fga = OpenFgaProvider()

    @Nested
    @DisplayName("writeRelationships")
    inner class WriteRelationship {
        @Test
        fun `write a new relationship`() {
            assertDoesNotThrow {
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
        }
    }
}
