package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.contract.INotificationEventsService
import co.bondspot.spbttest.domain.contract.INotificationObjectRepository
import co.bondspot.spbttest.domain.contract.INotificationSubscriptionService
import co.bondspot.spbttest.domain.entity.NotificationObject
import co.bondspot.spbttest.domain.event.NotificationNewEvent

class NotificationEventsService(
    val notifObjectRepo: INotificationObjectRepository,
    val notifSubService: INotificationSubscriptionService,
) : INotificationEventsService {
    override fun handle(e: NotificationNewEvent) {
        val subject = e.notifObjects.find { it.type == NotificationObject.Type.SUBJECT }

        if (subject != null) {
            val ftsResult = notifSubService.findAccounts(getEntityUid(subject))

            val notifObjects =
                ftsResult.accountsIds.mapNotNull { accountId ->
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

    private fun getEntityUid(notifObj: NotificationObject): String {
        return when (val entity = notifObj.entity) {
            NotificationObject.Entity.ACCOUNT -> "${entity.toString().lowercase()}_${notifObj.accountId}"
            NotificationObject.Entity.TASK -> "${entity.toString().lowercase()}_${notifObj.taskId}"
        }
    }
}
