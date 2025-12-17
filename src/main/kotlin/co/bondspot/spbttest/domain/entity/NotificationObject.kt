package co.bondspot.spbttest.domain.entity
/**
 * The individual data related to the notification.
 * As well for receiving state to be individually handled.
 * */
data class NotificationObject(
    val type: Type,
    /** notification listed to the receiver */
    val reached: Boolean = false,
    /** notification details expanded by the receiver */
    val expanded: Boolean = false,
    val notificationId: String? = null,
    val accountId: String? = null,
    val taskId: String? = null,
    val id: String? = null,
) {
    enum class Type {
        TRIGGER,
        RECEIVER,
        SUBJECT
    }
}
