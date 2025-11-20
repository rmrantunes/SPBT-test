package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.application.exception.ApplicationServiceException
import co.bondspot.spbttest.domain.contract.IAccountApplicationService
import co.bondspot.spbttest.domain.contract.IAccountRepository
import co.bondspot.spbttest.domain.contract.IIAMProvider
import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.IAMAccount
import co.bondspot.spbttest.domain.entity.IAMAuthenticatedToken
import co.bondspot.spbttest.domain.exception.IAMProviderException

open class AccountApplicationService(
    private val accountRepository: IAccountRepository, private val iamProvider: IIAMProvider
) : IAccountApplicationService {
    override fun register(account: Account, password: String) {
        val errorMessage = "Account already exists"

        var existingIAMAccount = iamProvider.getByUsername(account.username)
        if (existingIAMAccount != null) throw ApplicationServiceException(errorMessage).relatedHttpStatusCode { CONFLICT }

        var existingAccount = accountRepository.getByUsername(account.username)
        if (existingAccount != null) throw ApplicationServiceException(errorMessage).relatedHttpStatusCode { CONFLICT }

        existingIAMAccount = iamProvider.getByEmail(account.email)
        if (existingIAMAccount != null) throw ApplicationServiceException(errorMessage).relatedHttpStatusCode { CONFLICT }

        existingAccount = accountRepository.getByEmail(account.email)
        if (existingAccount != null) throw ApplicationServiceException(errorMessage).relatedHttpStatusCode { CONFLICT }

        val iamAccount = iamProvider.register(
            IAMAccount(
                account.username,
                account.email,
                account.firstName,
                account.lastName,
            ), password
        )

        val account = accountRepository.register(account.copy(iamAccountId = iamAccount.id))
        iamProvider.setExternalId(iamAccount.id!!, account.id!!)
    }

    override fun obtainAccessToken(
        username: String, password: String
    ): IAMAuthenticatedToken {
        return try {
            iamProvider.obtainAccessToken(username, password)
        } catch (e: IAMProviderException) {
            throw ApplicationServiceException(
                e.message ?: ""
            ).relatedHttpStatusCode { e.relatedHttpStatusCode }
        }
    }
}