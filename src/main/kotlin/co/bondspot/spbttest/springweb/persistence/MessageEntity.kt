package co.bondspot.spbttest.springweb.persistence

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
        fun fromDomain(entity: Message): MessageEntity = MessageEntity(
            text = entity.text,
            id = entity.id
        )
    }

    fun toDomain(): Message = Message(text, id)
}
