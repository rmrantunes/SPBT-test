package co.bondspot.spbttest.domain.event

import co.bondspot.spbttest.domain.entity.Task

data class TaskDetailsUpdatedEvent(val task: Task, val triggerAccountId: String)
