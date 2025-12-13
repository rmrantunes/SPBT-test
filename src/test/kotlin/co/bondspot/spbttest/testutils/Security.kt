package co.bondspot.spbttest.testutils

import java.util.UUID
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt

object AdminJwtMock {
    val jwtMock =
        Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claim("sub", UUID.randomUUID().toString())
            .claim("preferred_username", "zezindacaixapreta")
            .claim("email", "zezindacaixapreta@example.com")
            .build()!!

    val postProcessor =
        jwt().authorities(listOf(SimpleGrantedAuthority("ROLE_admin"))).jwt(jwtMock)!!
}

object AdminJwtMock2 {
    val jwtMock =
        Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claim("sub", UUID.randomUUID().toString())
            .claim("preferred_username", "aha")
            .claim("email", "aha@example.com")
            .build()!!

    val postProcessor =
        jwt().authorities(listOf(SimpleGrantedAuthority("ROLE_admin"))).jwt(jwtMock)!!
}
