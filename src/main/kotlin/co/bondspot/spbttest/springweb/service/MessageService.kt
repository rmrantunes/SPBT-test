package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.application.service.MessageApplicationService
import co.bondspot.spbttest.domain.signature.MessageRepositorySignature
import org.springframework.stereotype.Service

@Service
class MessageService(repo: MessageRepositorySignature) : MessageApplicationService(repo)