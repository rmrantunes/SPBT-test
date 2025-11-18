package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.Account

interface AccountRepositoryContract {
    fun register(account: Account): Account
    fun getByEmail(email: String): Account?
}