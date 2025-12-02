package co.bondspot.spbttest.domain.entity

data class IAMAuthenticatedToken(val token: String, val refreshToken: String, val expiresIn: Long)
