package co.bondspot.spbttest.infrastructure.iam

import co.bondspot.spbttest.domain.contract.IAMProviderContract
import co.bondspot.spbttest.domain.entity.IAMAccount
import co.bondspot.spbttest.domain.entity.IAMAuthenticatedToken
import co.bondspot.spbttest.domain.exception.IAMProviderException
import co.bondspot.spbttest.shared.enumeration.HttpStatusCode
import jakarta.ws.rs.NotFoundException
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.authorization.client.AuthzClient
import org.keycloak.authorization.client.Configuration
import org.keycloak.authorization.client.util.HttpResponseException
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation


class KeycloakIAMProviderException(message: String? = null, relatedHttpStatusCode: Int = 500) :
    IAMProviderException(message, relatedHttpStatusCode)

class KeycloakIAMProvider(
    private val serverUrl: String = "http://localhost:8080",
    private val clientSecret: String = "luJ0BaS4TtMK8tbK2AAwNKCtAM4Yd3Om",
    private val grantType: String = "client_credentials"
) : IAMProviderContract {
    private val externalIdAttrKey = "externalId"
    private val realm = "spbttest"
    private val clientId = "spbttest-api"
    private var keycloak = KeycloakBuilder
        .builder()
        .serverUrl(serverUrl)
        .realm(realm)
        .clientId(clientId)
        .clientSecret(clientSecret)
        .also {
            if (grantType == "password") it.password("5kbR0E2tzuUqJBKFzuFM")
        }
        .grantType(grantType)
        .build()

    var authzClient: AuthzClient = AuthzClient.create(
        Configuration(
            serverUrl,
            realm,
            clientId,
            mapOf("secret" to clientSecret),
            null
        )
    )


    fun close() {
        keycloak.close()
    }

    fun dangerouslyDeleteUser(id: String) {
        keycloak.realm(realm).users().delete(id).also { response -> response.close() }
    }

    override fun register(
        account: IAMAccount,
        password: String
    ): IAMAccount {
        val realm = keycloak.realm(realm)
        val userRepresentation = account.toUserRepresentation().apply { isEnabled = true }
        val response = realm.users().create(userRepresentation)

        var userId: String?

        when (response.status) {
            201 -> {
                userId = response.location.path.replace(Regex("([\\w+/]+)/"), "")
            }

            409 -> {
                response.close()
                throw KeycloakIAMProviderException("A user exists with same credentials", response.status)
            }

            else -> {
                response.close()
                throw Exception("Failed to create user: " + response.statusInfo)
            }
        }

        response.close()

        realm?.users()?.get(userId)?.resetPassword(CredentialRepresentation().also {
            it.value = password
            it.type = CredentialRepresentation.PASSWORD
            it.isTemporary = false
        })

        return account.copy(id = userId)
    }

    override fun obtainAccessToken(
        username: String,
        password: String
    ): IAMAuthenticatedToken {
        return try {
            val response = authzClient.obtainAccessToken(username, password)
            IAMAuthenticatedToken(response.token, response.refreshToken, response.expiresIn)
        } catch (e: HttpResponseException) {
            val (message, statusCode) = when (e.statusCode) {
                401 -> Pair("Invalid account credentials", e.statusCode)
                else -> Pair(e.message, e.statusCode)
            }

            throw KeycloakIAMProviderException(message, statusCode)
        } catch (e: Exception) {
            throw KeycloakIAMProviderException("Internal Error: ${e.message}")
        }
    }

    override fun getByEmail(email: String): IAMAccount? {
        val users = keycloak.realm(realm)?.users()?.searchByEmail(email, null)
        return users?.getOrNull(0)?.toIAMAccount()
    }

    override fun getByUsername(username: String): IAMAccount? {
        val users = keycloak.realm(realm)?.users()?.search(username, null)
        return users?.getOrNull(0)?.toIAMAccount()
    }

    override fun setExternalId(
        id: String,
        externalId: String
    ) {
        val userNotFoundMessage = "User not found"
        try {
            val user = keycloak.realm(realm)?.users()?.get(id)

            user?.update(
                UserRepresentation().also {
                    it.attributes = mapOf(externalIdAttrKey to listOf(it.id))
                }
            )
        } catch (_: NotFoundException) {
            throw KeycloakIAMProviderException(userNotFoundMessage, HttpStatusCode.NOT_FOUND)
        }
    }

    private fun UserRepresentation.toIAMAccount() = IAMAccount(
        username,
        email,
        firstName,
        lastName,
        id = attributes[externalIdAttrKey]?.get(0),
        externalId = id
    )

    private fun IAMAccount.toUserRepresentation(): UserRepresentation {
        return UserRepresentation().also {
            it.username = username
            it.email = email
            it.firstName = firstName
            it.lastName = lastName
            it.id = externalId
        }
    }
}
