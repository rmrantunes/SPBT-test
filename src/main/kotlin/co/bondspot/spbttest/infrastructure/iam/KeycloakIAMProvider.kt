package co.bondspot.spbttest.infrastructure.iam

import co.bondspot.spbttest.domain.contract.IIAMProvider
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
) : IIAMProvider {
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
        realmResource().users().delete(id).also { response -> response.close() }
    }

    override fun register(
        account: IAMAccount,
        password: String
    ): IAMAccount {
        requireThat(password.isNotEmpty()) { "password attribute must not be empty" }

        val realm = realmResource()
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
        requireThat(username.isNotEmpty()) { "username attribute must not be empty" }
        requireThat(password.isNotEmpty()) { "password attribute must not be empty" }

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
        requireThat(email.isNotEmpty()) { "email attribute must not be empty" }
        val users = realmResource()?.users()?.searchByEmail(email, null)
        return users?.getOrNull(0)?.toIAMAccount()
    }

    override fun getByUsername(username: String): IAMAccount? {
        requireThat(username.isNotEmpty()) { "username attribute must not be empty" }
        val users = realmResource()?.users()?.search(username, true)
        return users?.getOrNull(0)?.toIAMAccount()
    }

    override fun setExternalId(
        id: String,
        externalId: String
    ) {
        requireThat(id.isNotEmpty()) { "id attribute must not be empty" }
        requireThat(externalId.isNotEmpty()) { "externalId attribute must not be empty" }

        val userNotFoundMessage = "User not found"
        try {
            realmResource()?.users()?.searchByAttributes("$externalIdAttrKey:$externalId", true)?.run {
                if (isNotEmpty()) throw KeycloakIAMProviderException(
                    "A user already exists with same externalId",
                    HttpStatusCode.CONFLICT
                )
            }

            val user = realmResource().users().get(id)
            val userRepresentation = user.toRepresentation().also {
                it.attributes = mapOf(externalIdAttrKey to listOf(externalId))
            }
            // WARNING: updating without extending UserRepresentation instance can lead to
            // full wipe of the user record in Keycloak. Either don't forget about it, or try to
            // send a direct http request to Keycloak Admin REST API instead using SDK.
            // https://www.keycloak.org/docs-api/latest/rest-api/index.html#_users
            user?.update(userRepresentation)
        } catch (_: NotFoundException) {
            throw KeycloakIAMProviderException(userNotFoundMessage, HttpStatusCode.NOT_FOUND)
        }
    }

    private fun realmResource() = keycloak.realm(realm)

    private fun UserRepresentation.toIAMAccount(): IAMAccount {
        return IAMAccount(
            username,
            email,
            firstName,
            lastName,
            id = id,
            externalId = attributes?.get(externalIdAttrKey)?.get((0)),
        )
    }

    private fun IAMAccount.toUserRepresentation(): UserRepresentation {
        return UserRepresentation().also {
            it.username = username
            it.email = email
            it.firstName = firstName
            it.lastName = lastName
            it.id = id
        }
    }

    private fun requireThat(value: Boolean, message: () -> String) {
        if (!value) throw KeycloakIAMProviderException(message(), HttpStatusCode.INTERNAL_SERVER_ERROR)
    }
}
