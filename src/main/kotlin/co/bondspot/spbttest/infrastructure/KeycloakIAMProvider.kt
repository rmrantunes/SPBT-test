package co.bondspot.spbttest.infrastructure

import co.bondspot.spbttest.domain.contract.IAMProviderContract
import co.bondspot.spbttest.domain.entity.IAMAccount

class KeycloakIAMProvider : IAMProviderContract {
    override fun register(
        account: IAMAccount,
        password: String
    ): IAMAccount {
        TODO("Not yet implemented")
    }

    override fun authenticate(
        email: String,
        password: String
    ): Pair<String, String> {
        TODO("Not yet implemented")
    }

    override fun getByEmail(email: String): IAMAccount? {
        TODO("Not yet implemented")
    }

    override fun setExternalId(
        id: String,
        externalId: String
    ): IAMAccount {
        TODO("Not yet implemented")
    }
}