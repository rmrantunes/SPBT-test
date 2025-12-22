package co.bondspot.spbttest.domain.event

import co.bondspot.spbttest.domain.entity.Task

data class TaskDetailsUpdatedEvent(val newTask: Task, val oldTask: Task, val triggerAccountId: String)
