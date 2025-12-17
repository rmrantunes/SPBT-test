package co.bondspot.spbttest.domain.contract

interface IEventPublisher {
    fun publishEvent(e: Any)
}