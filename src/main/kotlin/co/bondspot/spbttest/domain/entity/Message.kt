package co.bondspot.spbttest.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "messages")
data class Message(val text: String, @Id @GeneratedValue(strategy = GenerationType.UUID) val id: String? = null)