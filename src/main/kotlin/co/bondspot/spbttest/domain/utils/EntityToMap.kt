package co.bondspot.spbttest.domain.utils

import co.bondspot.spbttest.domain.entity.Task
import java.time.LocalDateTime

fun Task.Companion.fromMap(map: Map<String, Any>): Task {
    return Task(
        id = map["id"].toString(),
        title = map["title"].toString(),
        status = map["status"]?.toString()?.let { Task.Status.valueOf(it) } ?: Task.Status.PENDING,
        description = map["description"].toString(),
        createdById = map["createdById"].toString(),
        lastUpdatedById = map["lastUpdatedById"].toString(),
        createdAt = map["createdAt"]?.toString()?.let { LocalDateTime.parse(it) },
        lastUpdatedAt = map["lastUpdatedAt"]?.toString()?.let { LocalDateTime.parse(it) },
    )
}

fun Task.toMap(): Map<String, Any> {
    return mapOf(
        "id" to this.id!!,
        "title" to this.title,
        "description" to this.description!!,
        "status" to this.status.toString(),
        "createdById" to this.createdById!!,
        "lastUpdatedById" to this.lastUpdatedById!!,
        "createdAt" to this.createdAt?.toString()!!,
        "lastUpdatedAt" to this.lastUpdatedAt?.toString()!!,
    )
}
