package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.application.service.TaskService
import co.bondspot.spbttest.domain.contract.IAccountRepository
import co.bondspot.spbttest.domain.contract.IEventPublisher
import co.bondspot.spbttest.domain.contract.IFgaProvider
import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import co.bondspot.spbttest.domain.contract.ITaskRepository
import org.springframework.stereotype.Service

@Service
class TaskService(
    taskRepo: ITaskRepository,
    accountRepo: IAccountRepository,
    fga: IFgaProvider,
    fts: IFullTextSearchProvider,
    eventPub: IEventPublisher,
) : TaskService(taskRepo, accountRepo, fga, fts, eventPub)
