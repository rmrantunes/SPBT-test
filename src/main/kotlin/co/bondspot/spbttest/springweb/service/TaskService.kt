package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.application.service.TaskApplicationService
import co.bondspot.spbttest.domain.signature.TaskRepositorySignature
import org.springframework.stereotype.Service

@Service
class TaskService (taskRepository: TaskRepositorySignature) : TaskApplicationService(taskRepository)