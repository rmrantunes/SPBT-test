package co.bondspot.spbttest.springweb.persistence

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import java.time.LocalDateTime
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
open class Auditable {
    @CreatedBy
    @Column(name = "created_by_id", nullable = false, updatable = false)
    var createdById: String? = null
    @LastModifiedBy @Column(name = "last_updated_by_id") var lastUpdatedById: String? = null
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null
    @LastModifiedDate @Column(name = "last_updated_at") var lastUpdatedAt: LocalDateTime? = null
}
