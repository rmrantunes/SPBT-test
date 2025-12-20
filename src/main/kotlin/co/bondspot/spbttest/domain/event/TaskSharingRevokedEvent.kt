package co.bondspot.spbttest.domain.event

import co.bondspot.spbttest.domain.entity.Task

data class TaskSharingRevokedEvent(
    val task: Task,
    val accountIdToRevokeFrom: String,
    val triggerAccountId: String,
)
