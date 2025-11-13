package co.bondspot.spbttest.application

import co.bondspot.spbttest.domain.contract.MessageApplicationServiceContract
import co.bondspot.spbttest.domain.contract.MessageRepositoryContract
import co.bondspot.spbttest.domain.entity.Message

open class MessageApplicationService (
    private val repository: MessageRepositoryContract
) : MessageApplicationServiceContract {
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