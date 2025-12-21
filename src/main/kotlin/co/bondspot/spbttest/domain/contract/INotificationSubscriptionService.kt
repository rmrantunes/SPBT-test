package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.NotificationSubscription

interface INotificationSubscriptionService {
    fun create(sub: NotificationSubscription)

    fun delete (id: String)
}
