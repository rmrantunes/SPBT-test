package co.bondspot.spbttest.domain.entity

import kotlinx.serialization.Serializable

// TODO @Serializable temporarily until proper Dto
@Serializable
data class Message(
    val text: String,
    val id: String? = null
)