package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.application.service.AccountApplicationService
import co.bondspot.spbttest.domain.contract.IAccountRepository
import co.bondspot.spbttest.domain.contract.IIAMProvider
import org.springframework.stereotype.Service

@Service
class AccountService(accountRepository: IAccountRepository, iamProvider: IIAMProvider) : AccountApplicationService(accountRepository, iamProvider)
