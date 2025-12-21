package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.contract.IFgaProvider
import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import co.bondspot.spbttest.domain.contract.INotificationObjectRepository
import co.bondspot.spbttest.domain.contract.INotificationRepository
import co.bondspot.spbttest.domain.contract.INotificationSubscriptionService
import co.bondspot.spbttest.domain.contract.ITaskEventsService
import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.FgaRelTuple
import co.bondspot.spbttest.domain.entity.Notification
import co.bondspot.spbttest.domain.entity.NotificationObject
import co.bondspot.spbttest.domain.entity.NotificationSubscription
import co.bondspot.spbttest.domain.entity.RevalRule
import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.domain.event.TaskNewEvent
import co.bondspot.spbttest.domain.event.TaskSharedEvent
import co.bondspot.spbttest.domain.event.TaskSharingRevokedEvent
import co.bondspot.spbttest.domain.event.TaskStatusUpdatedEvent

open class TaskEventsService(
    private val fga: IFgaProvider,
    private val fts: IFullTextSearchProvider,
    private val notifSubService: INotificationSubscriptionService,
    private val notifRepo: INotificationRepository,
    private val notifObjectRepo: INotificationObjectRepository,
) : ITaskEventsService {
    override fun handleTaskNewEvent(e: TaskNewEvent) {
        // TODO if error thrown in this block, remove created task, since no action could be
        //  done from any account.

        fga.writeRelationship(
            FgaRelTuple(
                Account.ENTITY_NAME to e.triggerAccountId,
                Task.FgaRelations.OWNER,
                Task.ENTITY_NAME to e.task.id!!,
            )
        )

        fts.index(Task.ENTITY_NAME, listOf(e.task))

        val entity = "${Task.ENTITY_NAME}:${e.task.id}"

        notifSubService.create(
            NotificationSubscription(
                accountId = e.triggerAccountId,
                type = NotificationSubscription.Type.ENTITY_EVENTS,
                events = listOf("*"),
                entityUid = entity,
                revalLevel = NotificationSubscription.RevalidationLevel.HIGH,
                revalRules = listOf(RevalRule("fga", mapOf("relation" to "owner"))),
            )
        )
    }

    override fun handleTaskUpdatedStatusEvent(e: TaskStatusUpdatedEvent) {
        // TODO implement resilience strategy to retry if DB is down

        val notifRoot =
            notifRepo.create(
                Notification(
                    Notification.Type.TASK_STATUS_UPDATED,
                    e.triggerAccountId,
                    mapOf("newStatus" to e.task.status, "taskTitle" to e.task.title),
                )
            )

        if (notifRoot.id != null) {
            notifObjectRepo.createMany(
                listOf(
                    NotificationObject(
                        type = NotificationObject.Type.TRIGGER,
                        entity = NotificationObject.Entity.ACCOUNT,
                        notificationId = notifRoot.id,
                        accountId = e.triggerAccountId,
                    ),
                    NotificationObject(
                        type = NotificationObject.Type.SUBJECT,
                        entity = NotificationObject.Entity.TASK,
                        notificationId = notifRoot.id,
                        taskId = e.task.id,
                    ),
                )
            )

            // TODO forward (async) NotificationNewEvent
        }
    }

    override fun handleTaskSharedEvent(e: TaskSharedEvent) {
        val entityUid = "${Task.ENTITY_NAME}:${e.task.id}"

        notifSubService.create(
            NotificationSubscription(
                accountId = e.accountIdToShareWith,
                type = NotificationSubscription.Type.ENTITY_EVENTS,
                events = listOf(Notification.Type.TASK_STATUS_UPDATED).map { it.toString() },
                entityUid = entityUid,
                revalLevel = NotificationSubscription.RevalidationLevel.HIGH,
                revalRules = listOf(RevalRule("fga", mapOf("relation" to Task.FgaRelations.VIEWER))),
            )
        )
    }

    override fun handleTaskSharingRevokedEvent(e: TaskSharingRevokedEvent) {
        val id = "${e.accountIdToRevokeFrom}_${Task.ENTITY_NAME}_${e.task.id}"
        notifSubService.delete(id)
    }
}
