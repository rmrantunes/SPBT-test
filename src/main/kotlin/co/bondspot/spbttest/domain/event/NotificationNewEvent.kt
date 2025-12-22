package co.bondspot.spbttest.domain.event

import co.bondspot.spbttest.domain.entity.Notification

data class NotificationNewEvent(
    val notification: Notification,
)
