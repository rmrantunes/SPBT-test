package co.bondspot.spbttest.infrastructure.iam

import co.bondspot.spbttest.KeycloakContainerExtension
import co.bondspot.spbttest.domain.entity.IAMAccount
import co.bondspot.spbttest.shared.enumeration.HttpStatusCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

private class KeycloakIAMProviderTests : KeycloakContainerExtension() {
    private lateinit var provider: KeycloakIAMProvider

    val inputAccount = IAMAccount(
        "myusername",
        "myusername.dev@example.com",
        "Rafael",
        "Antunes",
    )

    @BeforeEach
    fun setup() {
        provider = KeycloakIAMProvider(KEYCLOAK.authServerUrl)
    }

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

        @Test
        fun `return provider exception if a user exists with same credentials`() {
            val inputAccount2 = IAMAccount(
                "myusername",
                "myusername.dev@example.com",
                "Rafael",
                "Antunes",
            )

            assertDoesNotThrow {
                provider.register(
                    inputAccount2,
                    "rafAAA###123"
                )
            }

            val ex = assertThrows<KeycloakIAMProviderException> {
                provider.register(
                    inputAccount2,
                    "rafAAA###123"
                )
            }

            assertThat(ex.message).isEqualTo("A user exists with same credentials")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.CONFLICT)
        }
    }

    @Nested
    @DisplayName("when getting by email")
    inner class GetByEmail() {
        @Test
        fun `if no one found return null`() {
            val notFound = provider.getByEmail(inputAccount.email)
            assertThat(notFound).isNull()
        }

        @Test
        fun `return found Account`() {
            val inputAccount2 = inputAccount.copy(email = "fadfs3f@example.com", username = "zi3kanois")
            provider.register(
                inputAccount2,
                "rafAAA###123"
            )
            val account = provider.getByEmail(inputAccount2.email)
            assertThat(account?.id).isNotNull.isNotBlank
            assertThat(account?.email).isEqualTo(inputAccount2.email)
            assertThat(account?.firstName).isEqualTo(inputAccount2.firstName)
            assertThat(account?.lastName).isEqualTo(inputAccount2.lastName)
        }
    }
}