package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.Notification

interface INotificationRepository {
    fun create(notification: Notification): Notification
}