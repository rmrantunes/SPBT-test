package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import co.bondspot.spbttest.domain.contract.INotificationSubscriptionService
import co.bondspot.spbttest.domain.entity.NotificationSubscription
import co.bondspot.spbttest.domain.entity.NotificationSubscriptionFindAccounts

open class NotificationSubscriptionService(private val fts: IFullTextSearchProvider) :
    INotificationSubscriptionService {
    override fun create(sub: NotificationSubscription) {
        fts.index(NotificationSubscription.ENTITY_NAME, listOf(sub))
    }

    override fun delete(id: String) {
        fts.delete(NotificationSubscription.ENTITY_NAME, id)
    }

    override fun findAccounts(
        entityUid: String?,
        topics: List<String>?,
        events: List<String>?,
    ): NotificationSubscriptionFindAccounts {
        val filter = buildList {
            if (entityUid != null) add("entityUid = \"$entityUid\"")
            if (events != null && events.isNotEmpty()) add(events.map { "event = \"$it\"" })
        }

        // TODO we still need to be able to process (a possible) 1M accounts subscribed
        //  So we can add a pagination + some coroutines (and as well stream the http response to
        //  not load all objects at once)
        //  All that with retry.
        val result = fts.search(NotificationSubscription.ENTITY_NAME, "", filter = filter)

        return NotificationSubscriptionFindAccounts(result.hits.mapNotNull { hit -> hit["accountId"] as? String })
    }
}
