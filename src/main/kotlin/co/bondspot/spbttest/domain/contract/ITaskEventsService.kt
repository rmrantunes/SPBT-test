package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.event.UpdatedStatusTaskEvent

interface ITaskEventsService {
    fun handleUpdatedStatusTaskEvent(e: UpdatedStatusTaskEvent)
}
