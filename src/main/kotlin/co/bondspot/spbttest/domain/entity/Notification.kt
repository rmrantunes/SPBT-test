package co.bondspot.spbttest.domain.entity

data class Notification(
    val type: Type,
    val actionTriggerAccountId: String? = null,
    val isActionUndone: Boolean = false,
    val isAutomaticAction: Boolean = false,
    val id: String? = null,
) {
    enum class Type {
        TASK_STATUS_UPDATED
    }
}
