package co.bondspot.spbttest.infrastructure.iam

import co.bondspot.spbttest.domain.entity.IAMAccount
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class KeycloakIAMProviderTests {
    val provider = KeycloakIAMProvider()

    val inputAccount = IAMAccount(
        "myusername",
        "myusername.dev@example.com",
        "Rafael",
        "Antunes",
    )

    @AfterEach
    fun tearDownEach() {
        provider.close()
    }

    @Nested
    @DisplayName("when registering an account to keycloak")
    inner class RegisterAccount() {
        @Test
        fun `register with password and return Account`() {
            val createdAccount = provider.register(
                inputAccount,
                "rafAAA###123"
            )

            assertThat(createdAccount.externalId).isNotNull.isNotBlank
            assertThat(createdAccount.email).isEqualTo(inputAccount.email)
            assertThat(createdAccount.firstName).isEqualTo(inputAccount.firstName)
            assertThat(createdAccount.lastName).isEqualTo(inputAccount.lastName)
            assertThat(createdAccount.firstName).isEqualTo(inputAccount.firstName)
        }
    }
}