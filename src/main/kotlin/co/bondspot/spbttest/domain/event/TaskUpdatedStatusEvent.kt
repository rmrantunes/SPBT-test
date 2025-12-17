package co.bondspot.spbttest.domain.event

import co.bondspot.spbttest.domain.entity.Task

data class TaskUpdatedStatusEvent(val task: Task, val triggerAccountId: String)
