package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.IAMAccount
import co.bondspot.spbttest.domain.entity.IAMAuthenticatedToken

interface IAMProviderContract {
    fun register(account: IAMAccount, password: String): IAMAccount
    fun authenticate(username: String, password: String): IAMAuthenticatedToken
    fun getByEmail(email: String): IAMAccount?
    fun getByUsername(username: String): IAMAccount?
    fun setExternalId(id: String, externalId: String)
}