package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.application.service.AccountService
import co.bondspot.spbttest.domain.contract.IAccountRepository
import co.bondspot.spbttest.domain.contract.IIAMProvider
import org.springframework.stereotype.Service

@Service
class AccountService(accountRepository: IAccountRepository, iamProvider: IIAMProvider) :
    AccountService(accountRepository, iamProvider)
