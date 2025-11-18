package co.bondspot.spbttest.domain.entity

data class IAMAccount(
    val username: String,
    val email: String,
    val firstName: String = "",
    val lastName: String? = null,
    val id: String? = null,
    val externalId: String? = null,
)
