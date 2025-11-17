package co.bondspot.spbttest.domain.entity

data class Task(
    val title: String = "",
    val status: Status = Status.PENDING,
    val description: String? = null,
    val id: String? = null
) {
    enum class Status {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
    }
}