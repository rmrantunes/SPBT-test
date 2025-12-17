package co.bondspot.spbttest.domain.event

import co.bondspot.spbttest.domain.entity.Task

data class UpdatedStatusTaskEvent(val task: Task, val triggerAccountId: String)
