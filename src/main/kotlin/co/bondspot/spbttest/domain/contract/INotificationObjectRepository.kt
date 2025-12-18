package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.NotificationObject

interface INotificationObjectRepository {
    fun createMany(notifObject: List<NotificationObject>): List<NotificationObject>
}
