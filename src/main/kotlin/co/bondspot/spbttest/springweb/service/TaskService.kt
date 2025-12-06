package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.application.service.TaskApplicationService
import co.bondspot.spbttest.domain.contract.IAccountRepository
import co.bondspot.spbttest.domain.contract.IFgaProvider
import co.bondspot.spbttest.domain.contract.ITaskRepository
import org.springframework.stereotype.Service

@Service
class TaskService(
    taskRepository: ITaskRepository,
    accountRepository: IAccountRepository,
    fga: IFgaProvider,
) : TaskApplicationService(taskRepository, accountRepository, fga)
