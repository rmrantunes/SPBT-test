package co.bondspot.spbttest.domain.entity

import java.time.LocalDateTime

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
}
