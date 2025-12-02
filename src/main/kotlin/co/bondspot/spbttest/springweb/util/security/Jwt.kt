package co.bondspot.spbttest.springweb.util.security

import co.bondspot.spbttest.domain.entity.Account
import org.springframework.security.oauth2.jwt.Jwt

fun Jwt.toAccount(): Account {
    return Account(
        username = getClaimAsString("preferred_username"),
        email = getClaimAsString("email"),
        firstName = getClaimAsString("given_name") ?: "",
        lastName = getClaimAsString("family_name"),
        id = subject as String,
    )
}
