package co.bondspot.spbttest.springweb.configuration

import co.bondspot.spbttest.springweb.persistence.AccountEntity
import co.bondspot.spbttest.springweb.persistence.AccountRepository
import co.bondspot.spbttest.springweb.util.security.toAccount
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.http.MediaType
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationFilter
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

    @Autowired lateinit var accountRepository: AccountRepository

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(
                        "/message/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                    )
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }
            .oauth2ResourceServer { oauth2Server ->
                oauth2Server.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(CustomJwtAuthenticationConverter())
                }
                oauth2Server.authenticationEntryPoint(CustomAuthenticationEntryPoint())
            }
            .exceptionHandling { exceptionHandling ->
                exceptionHandling.authenticationEntryPoint(CustomAuthenticationEntryPoint())
            }

        http.addFilterAfter(
            CreateAccountFromJwtFilter(accountRepository),
            AuthenticationFilter::class.java,
        )

        return http.build()
    }
}

class CreateAccountFromJwtFilter(private val accountRepository: AccountRepository) :
    OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val jwt = SecurityContextHolder.getContext().authentication?.principal as? Jwt
            if (
                jwt != null &&
                    !jwt.subject.isNullOrBlank() &&
                    accountRepository.findById(jwt.subject).isEmpty
            ) {
                // TODO update existing app record fields (email, username, first_name, last_name)
                // from incoming jwt data
                accountRepository.save(AccountEntity.fromDomain(jwt.toAccount()))
            }
        } finally {
            filterChain.doFilter(request, response)
        }
    }
}

private class CustomAuthenticationEntryPoint : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException?,
    ) {

        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write(
            """
                    {
                        "errors": [
                            {
                                "message": "Full authentication is required to access this resource. Check credentials validity. Check the available authentication methods in the docs.",
                                "type": "INVALID_CREDENTIALS"
                            }
                        ],
                        "statusCode": ${HttpServletResponse.SC_UNAUTHORIZED}
                    }
                """
                .trimIndent()
        )
    }
}

@Component
class CustomJwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken> {
    private val defaultGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()

    override fun convert(jwt: Jwt): AbstractAuthenticationToken? {
        val grantedAuthorities = defaultGrantedAuthoritiesConverter.convert(jwt)
        val kcRealmRolesMap = jwt.getClaim<Map<String, List<String>>>("realm_access")
        kcRealmRolesMap["roles"]?.forEach {
            grantedAuthorities?.add(SimpleGrantedAuthority("ROLE_$it"))
        }
        println("grantedAuthorities: $grantedAuthorities")
        return JwtAuthenticationToken(jwt, grantedAuthorities, jwt.subject)
    }
}
