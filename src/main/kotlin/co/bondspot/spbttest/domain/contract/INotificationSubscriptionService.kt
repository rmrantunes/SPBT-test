package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.NotificationSubscription
import co.bondspot.spbttest.domain.entity.NotificationSubscriptionFindAccounts

interface INotificationSubscriptionService {
    fun create(sub: NotificationSubscription)

    fun delete(id: String)

    fun findAccounts(
        entityUid: String? = null,
        topics: List<String>? = null,
        events: List<String>? = listOf("*"),
    ): NotificationSubscriptionFindAccounts
}
