package co.bondspot.spbttest.domain.signature

import co.bondspot.spbttest.domain.entity.Message

interface MessageRepositorySignature {
    fun save(message: Message): Message
    fun find(): List<Message>
    fun findById(id: String): Message?
}