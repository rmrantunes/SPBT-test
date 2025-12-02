package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.IAMAccount
import co.bondspot.spbttest.domain.entity.IAMAuthenticatedToken

interface IIAMProvider {
    fun register(account: IAMAccount, password: String): IAMAccount

    fun obtainAccessToken(username: String, password: String): IAMAuthenticatedToken

    fun getByEmail(email: String): IAMAccount?

    fun getByUsername(username: String): IAMAccount?

    fun setExternalId(id: String, externalId: String)
}
