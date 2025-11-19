package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.IAMAuthenticatedToken

interface IAccountApplicationService {
    fun register(account: Account, password: String)
    fun obtainAccessToken(username: String, password: String): IAMAuthenticatedToken
}