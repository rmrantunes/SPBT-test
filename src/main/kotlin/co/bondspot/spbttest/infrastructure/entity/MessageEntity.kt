package co.bondspot.spbttest.infrastructure.entity

import co.bondspot.spbttest.domain.entity.Message
import jakarta.persistence.*

@Entity
@Table(name = "messages")
data class MessageEntity(
    @Column(nullable = false)
    val text: String,
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null
) {
    companion object {
        fun fromDomainEntity(entity: Message): MessageEntity = MessageEntity(
            text = entity.text,
            id = entity.id
        )
    }

    fun toDomainEntity(): Message = Message(text, id)
}
