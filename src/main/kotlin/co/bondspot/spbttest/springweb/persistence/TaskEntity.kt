package co.bondspot.spbttest.springweb.persistence

import co.bondspot.spbttest.domain.entity.Task
import jakarta.persistence.*

@Entity
@Table(name = "task")
data class TaskEntity(
    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val status: Task.Status = Task.Status.PENDING,

    @Id @GeneratedValue(strategy = GenerationType.UUID) val id: String? = null,
) {
    companion object {
        fun fromDomain(task: Task): TaskEntity = TaskEntity(task.title, task.status, task.id)
    }

    fun toDomain(): Task = Task(title, status, id)
}
