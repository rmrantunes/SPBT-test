package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.event.NotificationNewEvent

interface INotificationEventsService {
    fun handle(e: NotificationNewEvent)
}
