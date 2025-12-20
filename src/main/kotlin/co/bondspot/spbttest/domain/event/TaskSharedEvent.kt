package co.bondspot.spbttest.domain.event

import co.bondspot.spbttest.domain.entity.Task

data class TaskSharedEvent(
    val task: Task,
    val accountIdToShareWith: String,
    val triggerAccountId: String,
)
