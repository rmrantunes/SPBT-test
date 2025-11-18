package co.bondspot.spbttest.domain.entity

data class Account(
    val username: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String? = null,
    val iamAccountId: String? = null,
    val id: String? = null,
) {
//    init {
//        if (!__isPartialEntity) {
//            require(id != null && runCatching { UUID.fromString(id) }.isSuccess) { "Account id must be a UUID" }
//            require(email.isNotBlank()) { "Account email must not be blank" }
//        }
//    }
}