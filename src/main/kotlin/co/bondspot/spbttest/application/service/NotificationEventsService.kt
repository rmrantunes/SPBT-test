package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.contract.INotificationEventsService
import co.bondspot.spbttest.domain.event.NotificationNewEvent

class NotificationEventsService : INotificationEventsService {
    override fun handle(e: NotificationNewEvent) {
        TODO("Not yet implemented")
    }
}