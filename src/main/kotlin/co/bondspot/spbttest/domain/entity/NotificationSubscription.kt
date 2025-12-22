package co.bondspot.spbttest.domain.entity

import java.time.LocalDateTime
import java.util.UUID

data class RevalRule(val name: String, val params: Map<String, Any?>)

data class NotificationSubscription(
    val type: Type,
    val accountId: String,
    /** Notification.Type, wildcard "*" or any string. Allowed events of type (entity or topic). */
    val events: List<String>,
    /**
     * Topics or keywords the accounts can be subscribed to. It's a list, but you may think one
     * record for each topic more manageable.
     */
    val topics: List<String>? = null,
    /** Entity the account is directly subscribed to. Concatenation of `entity_name:entity_id` */
    val entityUid: String? = null,
    /**
     * To check if account's going to receive a certain notification based on their custom params
     * (this) compared to event params (future query input).
     */
    val params: List<Map<String, Any?>>? = null,
    /**
     * The property controls how inconsistencies be accepted. Those the notification subscription
     * record couldn't keep track with the business logic rules for subscription, for example, a
     * user access to a task was revoked and their subscription wasn't disabled/deleted due to
     * infrastructure failure.
     *
     * Since we can't run the rule evaluation for each subscription on deliverance, revalidation
     * means that expired records will cause failure in the deliverance queue and go to a rule
     * evaluation queue, to check if account can indeed receive the notification, and then be sent
     * back to the deliverance queue or be disabled/deleted.
     *
     * The rule evaluation is based on revalRules params.
     *
     * Default: `VERY_LOW`
     */
    val revalLevel: RevalidationLevel = RevalidationLevel.VERY_LOW,
    val revalRules: List<RevalRule> = emptyList(),
    val createdAt: String? = LocalDateTime.now().toString(),
    val id: String =
        if (type == Type.ENTITY_EVENTS) "${accountId}_${entityUid}".replace(":", "_")
        else UUID.randomUUID().toString(),
) {

    enum class Type {
        ENTITY_EVENTS,
        TOPIC_EVENTS,
    }

    enum class RevalidationLevel {
        /** 25min of expiration */
        HIGH,
        /** 1h of expiration */
        NORMAL,
        /** 24h of expiration */
        LOW,
        /** 48h of expiration */
        VERY_LOW,
        NEVER,
    }

    val revalAt: String? =
        when (revalLevel) {
            RevalidationLevel.HIGH -> LocalDateTime.now().plusMinutes(25L).toString()
            RevalidationLevel.NORMAL -> LocalDateTime.now().plusHours(1L).toString()
            RevalidationLevel.LOW -> LocalDateTime.now().plusHours(24L).toString()
            RevalidationLevel.VERY_LOW -> LocalDateTime.now().plusHours(48L).toString()
            RevalidationLevel.NEVER -> null
        }

    companion object {
        const val ENTITY_NAME = "notif_sub"
    }
}

data class NotificationSubscriptionFindAccounts(
    val accountsIds: List<String>,
)
