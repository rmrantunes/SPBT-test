package co.bondspot.spbttest.springweb.persistence

import co.bondspot.spbttest.domain.entity.Task
import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Entity
@Table(name = "task")
class TaskEntity(
    @Column(nullable = false) val title: String,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnDefault("PENDING")
    val status: Task.Status = Task.Status.PENDING,
    @Column(length = 1000) val description: String? = null,
    @Id @GeneratedValue(strategy = GenerationType.UUID) val id: String? = null,
) : Auditable() {
    companion object {
        fun fromDomain(task: Task) =
            TaskEntity(task.title, task.status, task.description, id = task.id)
    }

    fun toDomain() =
        Task(
            title,
            status,
            description,
            createdById,
            lastUpdatedById,
            createdAt,
            lastUpdatedAt,
            id = id,
        )
}

@Repository interface TaskRepository : JpaRepository<TaskEntity, String>
