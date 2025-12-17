package co.bondspot.spbttest.domain.entity

data class Notification(
    val type: Type,
    val undone: Boolean = false,
    val id: String? = null,
) {
    enum class Type {
        TASK_STATUS_UPDATED
    }
}
