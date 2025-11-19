package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.IAMAuthenticatedToken

interface AccountApplicationServiceContract {
    fun register(account: Account, password: String)
    fun authenticate(email: String, password: String): IAMAuthenticatedToken
}