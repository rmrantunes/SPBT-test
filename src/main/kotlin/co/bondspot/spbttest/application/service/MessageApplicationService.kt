package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.contract.IMessageApplicationService
import co.bondspot.spbttest.domain.contract.IMessageRepository
import co.bondspot.spbttest.domain.entity.Message

open class MessageApplicationService(private val repository: IMessageRepository) :
    IMessageApplicationService {
    override fun save(message: Message): Message {
        return repository.save(message)
    }

    override fun find(): List<Message> {
        return repository.find()
    }

    override fun findById(id: String): Message? {
        return repository.findById(id)
    }
}
