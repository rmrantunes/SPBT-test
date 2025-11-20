package co.bondspot.spbttest.infrastructure.iam

import co.bondspot.spbttest.KeycloakContainerExtension
import co.bondspot.spbttest.domain.entity.IAMAccount
import co.bondspot.spbttest.shared.enumeration.HttpStatusCode
import co.bondspot.spbttest.testutils.KSelect
import org.assertj.core.api.Assertions.assertThat
import org.instancio.Instancio
import org.junit.jupiter.api.*
import java.util.*

private class KeycloakIAMProviderTests() : KeycloakContainerExtension() {
    private lateinit var provider: KeycloakIAMProvider

    private val password = "1#1#pass__sup3rst00ng$"

    private fun generateInputAccount() = Instancio.of(IAMAccount::class.java)
        .generate(KSelect.field(IAMAccount::username)) { it.text().word().adjective().noun() }
        .generate(KSelect.field(IAMAccount::email)) { it.net().email() }
        .generate(KSelect.field(IAMAccount::firstName)) { it.text().word().adjective() }
        .generate(KSelect.field(IAMAccount::lastName)) { it.text().word().noun() }
        .create()

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
            val inputAccount = generateInputAccount()

            val createdAccount = provider.register(inputAccount, password)
            assertThat(createdAccount.id).isNotNull.isNotBlank
            assertThat(createdAccount.email).isEqualTo(inputAccount.email)
            assertThat(createdAccount.firstName).isEqualTo(inputAccount.firstName)
            assertThat(createdAccount.lastName).isEqualTo(inputAccount.lastName)
            assertThat(createdAccount.firstName).isEqualTo(inputAccount.firstName)
        }

        @Test
        fun `return provider exception if a user exists with same credentials`() {
            val inputAccount = generateInputAccount()

            assertDoesNotThrow {
                provider.register(inputAccount, password)
            }

            val ex = assertThrows<KeycloakIAMProviderException> {
                provider.register(inputAccount, password)
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
            val notFound = provider.getByEmail("notanemail@example.com")
            assertThat(notFound).isNull()
        }

        @Test
        fun `return found Account`() {
            val inputAccount = generateInputAccount()
            provider.register(inputAccount, password)
            val account = provider.getByEmail(inputAccount.email)
            assertThat(account?.id).isNotNull.isNotBlank
            assertThat(account?.email).isEqualTo(inputAccount.email)
            assertThat(account?.firstName).isEqualTo(inputAccount.firstName)
            assertThat(account?.lastName).isEqualTo(inputAccount.lastName)
        }
    }

    @Nested
    @DisplayName("when getting by username...")
    inner class GetByUsername() {
        @Test
        fun `if no one found return null`() {
            val notFound = provider.getByUsername("not_me")
            assertThat(notFound).isNull()
        }

        @Test
        fun `return found Account`() {
            val inputAccount = generateInputAccount()
            provider.register(inputAccount, password)
            val account = provider.getByUsername(inputAccount.username)
            assertThat(account?.id).isNotNull.isNotBlank
            assertThat(account?.email).isEqualTo(inputAccount.email)
            assertThat(account?.firstName).isEqualTo(inputAccount.firstName)
            assertThat(account?.lastName).isEqualTo(inputAccount.lastName)
        }
    }

    @Nested
    @DisplayName("when setting externalId...")
    inner class SetExternalId() {
        @Test
        fun `throw not found exception if no user with id`() {
            val ex = assertThrows<KeycloakIAMProviderException> {
                provider.setExternalId("not_an_id", "some-external-id")
            }

            assertThat(ex.message).isEqualTo("User not found")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.NOT_FOUND)
        }

        @Test
        fun `if found update set user external id successfully`() {
            val inputAccount = generateInputAccount()
            val account = provider.register(inputAccount, password)

            val externalId = UUID.randomUUID().toString()

            assertDoesNotThrow { provider.setExternalId(account.id!!, externalId) }

            val updatedAccount = provider.getByEmail(inputAccount.email)

            assertThat(updatedAccount?.externalId).isEqualTo(externalId)
        }
    }

    @Nested
    @DisplayName("when authenticating with password...")
    inner class AuthenticateByPassword() {
        @Test
        fun `if valid return access token and refresh token`() {
            val inputAccount = generateInputAccount()
            provider.register(inputAccount, password)

            val response = provider.obtainAccessToken(inputAccount.username, password)

            assertThat(response.token).isNotNull.isNotBlank
            assertThat(response.refreshToken).isNotNull.isNotBlank
            assertThat(response.expiresIn).isNotNull.isPositive
        }

        @Test
        fun `if invalid username throw exception`() {
            val response =
                assertThrows<KeycloakIAMProviderException> { provider.obtainAccessToken("not_the_username", password) }

            assertThat(response.message).isEqualTo("Invalid account credentials")
            assertThat(response.relatedHttpStatusCode).isEqualTo(401)
        }

        @Test
        fun `if invalid password throw exception`() {
            val inputAccount = generateInputAccount()
            provider.register(inputAccount, password)
            val response = assertThrows<KeycloakIAMProviderException> {
                provider.obtainAccessToken(
                    inputAccount.username,
                    "not_the_password"
                )
            }

            assertThat(response.message).isEqualTo("Invalid account credentials")
            assertThat(response.relatedHttpStatusCode).isEqualTo(401)
        }
    }
}
