package co.bondspot.spbttest.domain.event

import co.bondspot.spbttest.domain.entity.Notification
import co.bondspot.spbttest.domain.entity.NotificationObject

data class NotificationNewEvent(
    val notification: Notification,
    val notifObjects: List<NotificationObject> = listOf(),
)
