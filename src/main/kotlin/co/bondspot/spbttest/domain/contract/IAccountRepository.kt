package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.Account

interface IAccountRepository {
    fun register(account: Account): Account

    fun getByEmail(email: String): Account?

    fun getByUsername(username: String): Account?
}
