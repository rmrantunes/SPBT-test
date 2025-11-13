package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.application.MessageApplicationService
import co.bondspot.spbttest.domain.contract.MessageRepositoryContract
import org.springframework.stereotype.Service

@Service
class MessageService(repo: MessageRepositoryContract) : MessageApplicationService(repo)