package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import co.bondspot.spbttest.domain.contract.INotificationEventsService
import co.bondspot.spbttest.domain.contract.INotificationObjectRepository
import co.bondspot.spbttest.domain.entity.NotificationObject
import co.bondspot.spbttest.domain.event.NotificationNewEvent

class NotificationEventsService(
    val notifObjectRepo: INotificationObjectRepository,
    val fts: IFullTextSearchProvider,
) : INotificationEventsService {
    override fun handle(e: NotificationNewEvent) {
        val subject = e.notifObjects.find { it.type == NotificationObject.Type.SUBJECT }

        if (subject != null) {
            val ftsResult =
                fts.search(subject.entity.toString().lowercase(), "", listOf(subject.taskId!!))

            val notifObjects =
                ftsResult.hits.mapNotNull {
                    val accountId = it["accountId"] as? String ?: return@mapNotNull null
                    if (accountId == e.notification.actionTriggerAccountId) return@mapNotNull null

                    NotificationObject(
                        type = NotificationObject.Type.RECEIVER,
                        entity = NotificationObject.Entity.ACCOUNT,
                        accountId = accountId,
                        notificationId = e.notification.id!!,
                    )
                }

            notifObjectRepo.createMany(notifObjects)
        }
    }
}
