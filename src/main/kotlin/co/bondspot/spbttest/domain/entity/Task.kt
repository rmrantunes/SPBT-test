package co.bondspot.spbttest.domain.entity

import java.time.LocalDateTime

object TaskFgaRelations {
    const val OWNER = "owner"
    const val VIEWER = "viewer"
    const val EDITOR = "editor"
    const val CAN_EDIT_DETAILS = "can_edit_details"
    const val CAN_EDIT_STATUS = "can_edit_status"
}

data class Task(
    val title: String = "",
    val status: Status = Status.PENDING,
    val description: String? = null,
    val createdById: String? = null,
    val lastUpdatedById: String? = null,
    val createdAt: LocalDateTime? = null,
    val lastUpdatedAt: LocalDateTime? = null,
    val id: String? = null,
) {
    enum class Status {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
    }

    companion object {
        val FgaRelations = TaskFgaRelations
    }
}
