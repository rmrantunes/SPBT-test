package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.Account

interface AccountApplicationServiceContract {
    fun register(account: Account, password: String)
    fun authenticate(email: String, password: String): Pair<String, String>
}