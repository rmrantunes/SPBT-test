package co.bondspot.spbttest.domain.event

import co.bondspot.spbttest.domain.entity.Task

data class TaskNewEvent(val task: Task, val triggerAccountId: String)
