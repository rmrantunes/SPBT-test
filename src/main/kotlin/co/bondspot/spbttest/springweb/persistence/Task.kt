package co.bondspot.spbttest.springweb.persistence

import co.bondspot.spbttest.domain.entity.Task
import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Entity
@Table(name = "task")
data class TaskEntity(
    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnDefault("PENDING")
    val status: Task.Status = Task.Status.PENDING,

    @Column(length = 1000)
    val description: String? = null,

    @Id @GeneratedValue(strategy = GenerationType.UUID) val id: String? = null,
) {
    companion object {
        fun fromDomain(task: Task): TaskEntity = TaskEntity(task.title, task.status, task.description, task.id)
    }

    fun toDomain(): Task = Task(title, status, description, id)
}

@Repository
interface TaskRepository : JpaRepository<TaskEntity, String>