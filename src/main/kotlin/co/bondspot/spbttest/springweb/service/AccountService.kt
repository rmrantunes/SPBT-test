package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.application.service.AccountApplicationService
import co.bondspot.spbttest.domain.contract.AccountRepositoryContract
import co.bondspot.spbttest.domain.contract.IAMProviderContract
import org.springframework.stereotype.Service

@Service
class AccountService(accountRepository: AccountRepositoryContract, iamProvider: IAMProviderContract) : AccountApplicationService(accountRepository, iamProvider)
