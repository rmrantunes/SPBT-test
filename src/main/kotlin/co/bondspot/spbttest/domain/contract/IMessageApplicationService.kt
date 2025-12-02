package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.Message

interface IMessageApplicationService {
    fun save(message: Message): Message

    fun find(): List<Message>

    fun findById(id: String): Message?
}
