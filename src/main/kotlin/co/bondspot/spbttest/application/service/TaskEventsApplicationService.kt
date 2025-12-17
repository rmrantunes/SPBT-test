package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.contract.ITaskEventsService
import co.bondspot.spbttest.domain.event.UpdatedStatusTaskEvent

class TaskEventsApplicationService : ITaskEventsService {
    override fun handleUpdatedStatusTaskEvent(e: UpdatedStatusTaskEvent) {
        TODO("Not yet implemented")
    }
}
