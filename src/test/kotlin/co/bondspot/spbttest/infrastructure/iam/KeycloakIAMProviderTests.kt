package co.bondspot.spbttest.infrastructure.iam

import co.bondspot.spbttest.KeycloakContainerExtension
import co.bondspot.spbttest.domain.entity.IAMAccount
import co.bondspot.spbttest.shared.enumeration.HttpStatusCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.util.*
import kotlin.test.Ignore

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
            val inputAccount = IAMAccount(
                "myusernam222e",
                "myusern2222ame.dev@example.com",
                "Rafael",
                "Antunes",
            )

            val createdAccount = provider.register(
                inputAccount,
                "rafAAA###123"
            )
            assertThat(createdAccount.id).isNotNull.isNotBlank
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

    @Nested
    @DisplayName("when getting by username...")
    inner class GetByUsername() {
        @Test
        fun `if no one found return null`() {
            val notFound = provider.getByUsername(inputAccount.username)
            assertThat(notFound).isNull()
        }

        @Test
        fun `return found Account`() {
            val inputAccount2 = inputAccount.copy(email = "noloseeeee@example.com", username = "zi3kanois3333")
            provider.register(
                inputAccount2,
                "rafAAA###123"
            )
            val account = provider.getByUsername(inputAccount2.username)
            assertThat(account?.id).isNotNull.isNotBlank
            assertThat(account?.email).isEqualTo(inputAccount2.email)
            assertThat(account?.firstName).isEqualTo(inputAccount2.firstName)
            assertThat(account?.lastName).isEqualTo(inputAccount2.lastName)
        }
    }


    @Nested
    @DisplayName("when setting externalId...")
    inner class SetExternalId() {
        @Test
        fun `throw not found exception if no user with id`() {
            val ex = assertThrows<KeycloakIAMProviderException> {
                provider.setExternalId("fdsf", "some-external-id")
            }

            assertThat(ex.message).isEqualTo("User not found")
            assertThat(ex.relatedHttpStatusCode).isEqualTo(HttpStatusCode.NOT_FOUND)
        }

        @Ignore
        fun `if found update set user external id successfully`() {
            val inputAccount = inputAccount.copy(email = "idontnou@example.com", username = "heheffafa")
            val password = "rafAAA###123"
            val account = provider.register(inputAccount, password)

            val externalId = UUID.randomUUID().toString()

            assertDoesNotThrow { provider.setExternalId(account.id!!, password) }

            val updatedAccount = provider.getByEmail(inputAccount.email)

            assertThat(updatedAccount?.externalId).isEqualTo(externalId)
        }
    }

    @Nested
    @DisplayName("when authenticating with password...")
    inner class AuthenticateByPassword() {
        @Test
        fun `if valid return access token and refresh token`() {
            val inputAccount = inputAccount.copy(email = "fadfs3sf@example.com", username = "zi3kasnois")
            val password = "rafAAA###123"
            provider.register(
                inputAccount,
                password
            )

            val response = provider.obtainAccessToken(inputAccount.username, password)

            assertThat(response.token).isNotNull.isNotBlank
            assertThat(response.refreshToken).isNotNull.isNotBlank
            assertThat(response.expiresIn).isNotNull.isPositive
        }

        @Test
        fun `if invalid username throw exception`() {
            val password = "rafAAA###123"
            val response =
                assertThrows<KeycloakIAMProviderException> { provider.obtainAccessToken("not_the_username", password) }

            assertThat(response.message).isEqualTo("Invalid account credentials")
            assertThat(response.relatedHttpStatusCode).isEqualTo(401)
        }

        @Test
        fun `if invalid password throw exception`() {
            val inputAccount = inputAccount.copy(email = "hahadfsd@example.com", username = "faskjdflks")
            val password = "rafAAA###123"
            provider.register(
                inputAccount,
                password
            )
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