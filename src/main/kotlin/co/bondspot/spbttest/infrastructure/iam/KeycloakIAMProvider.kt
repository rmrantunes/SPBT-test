package co.bondspot.spbttest.infrastructure.iam

import co.bondspot.spbttest.domain.contract.IAMProviderContract
import co.bondspot.spbttest.domain.entity.IAMAccount
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation

class KeycloakIAMProvider : IAMProviderContract {
    private val realm = "spbttest"
    private var keycloak = KeycloakBuilder
        .builder()
        .serverUrl("http://localhost:8080")
        .realm(realm)
        .clientId("spbttest-api")
        .clientSecret("luJ0BaS4TtMK8tbK2AAwNKCtAM4Yd3Om")
        .username("realm-admin")
        .password("5kbR0E2tzuUqJBKFzuFM")
        .build()

    fun close() {
        keycloak.close()
    }

    override fun register(
        account: IAMAccount,
        password: String
    ): IAMAccount {
        val realm = keycloak.realm(realm)
        val userRepresentation = account.toUserRepresentation().apply { isEnabled = true }
        val response = realm.users().create(userRepresentation)

        var userId: String?

        if (response.status == 201) {
            userId = response.location.path.replace(Regex("([\\w+/]+)/"), "")
        } else {
            throw Exception("Failed to create user: " + response.statusInfo)
        }

        response.close()

        realm?.users()?.get(userId)?.resetPassword(CredentialRepresentation().also {
            it.value = password
            it.type = CredentialRepresentation.PASSWORD
            it.isTemporary = false
        })

        return account.copy(externalId = userId)
    }

    override fun authenticate(
        email: String,
        password: String
    ): Pair<String, String> {
        TODO("Not yet implemented")
    }

    override fun getByEmail(email: String): IAMAccount? {
        val users = keycloak.realm(realm)?.users()?.searchByEmail(email, null)
        return users?.getOrNull(0)?.toIAMAccount()
    }

    override fun setExternalId(
        id: String,
        externalId: String
    ) {
        keycloak.realm(realm)?.users()?.get(id)?.update(
            UserRepresentation().also {
                it.attributes["externalId"] = listOf(it.id)
            }
        )
    }

    private fun UserRepresentation.toIAMAccount() = IAMAccount(
        username,
        email,
        firstName,
        lastName,
        externalId = id,
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