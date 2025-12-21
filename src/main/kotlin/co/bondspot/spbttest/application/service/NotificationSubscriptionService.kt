package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import co.bondspot.spbttest.domain.contract.INotificationSubscriptionService
import co.bondspot.spbttest.domain.entity.NotificationSubscription

open class NotificationSubscriptionService(private val fts: IFullTextSearchProvider) :
    INotificationSubscriptionService {
    override fun create(sub: NotificationSubscription) {
        fts.index(NotificationSubscription.ENTITY_NAME, listOf(sub))
    }

    override fun delete(id: String) {
        TODO("Not yet implemented")
    }
}
