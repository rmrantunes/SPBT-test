package co.bondspot.spbttest.infrastructure.fga

import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.FgaRelTuple
import co.bondspot.spbttest.domain.entity.Task
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.*
import kotlin.test.Test

class OpenFgaProviderTests {
    private val accountId = UUID.randomUUID().toString()
    private val taskId = UUID.randomUUID().toString()

    @Nested
    @DisplayName("writeRelationships")
    inner class WriteRelationship {
        val fga = mockk<OpenFgaProvider>()
        @Test
        fun `write a new relationship`() {
            every { fga.writeRelationships(any()) } returns Unit

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
