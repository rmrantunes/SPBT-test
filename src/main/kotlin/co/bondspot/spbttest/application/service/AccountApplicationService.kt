package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.application.exception.ApplicationServiceException
import co.bondspot.spbttest.domain.contract.AccountApplicationServiceContract
import co.bondspot.spbttest.domain.contract.AccountRepositoryContract
import co.bondspot.spbttest.domain.contract.IAMProviderContract
import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.IAMAccount

open class AccountApplicationService(
    private val accountRepositoryContract: AccountRepositoryContract,
    private val iamProviderContract: IAMProviderContract
) :
    AccountApplicationServiceContract {

    override fun register(account: Account, password: String) {
        val errorMessage = "Account already exists"

        var existingIAMAccount = iamProviderContract.getByUsername(account.username)
        if (existingIAMAccount != null) throw ApplicationServiceException(errorMessage).relatedHttpStatusCode { CONFLICT }

        var existingAccount = accountRepositoryContract.getByUsername(account.username)
        if (existingAccount != null) throw ApplicationServiceException(errorMessage).relatedHttpStatusCode { CONFLICT }

        existingIAMAccount = iamProviderContract.getByEmail(account.email)
        if (existingIAMAccount != null) throw ApplicationServiceException(errorMessage).relatedHttpStatusCode { CONFLICT }

        existingAccount = accountRepositoryContract.getByEmail(account.email)
        if (existingAccount != null) throw ApplicationServiceException(errorMessage).relatedHttpStatusCode { CONFLICT }

        val iamAccount = iamProviderContract.register(
            IAMAccount(
                account.username,
                account.email,
                account.firstName,
                account.lastName,
            ),
            password
        )

        val account = accountRepositoryContract.register(account.copy(iamAccountId = iamAccount.id))
        iamProviderContract.setExternalId(iamAccount.id!!, account.id!!)
    }

    override fun authenticate(
        email: String,
        password: String
    ): Pair<String, String> {
        return iamProviderContract.authenticate(email, password)
    }
}