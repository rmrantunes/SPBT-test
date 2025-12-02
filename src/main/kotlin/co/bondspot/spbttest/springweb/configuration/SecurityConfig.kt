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
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests { authorize ->
            authorize.requestMatchers("/auth/**").permitAll().anyRequest().authenticated()
        }.oauth2ResourceServer { oauth2Server -> oauth2Server.jwt(Customizer.withDefaults()) }

        http.addFilterAfter(CreateAccountFromJwtFilter(accountRepository), AuthenticationFilter::class.java)

        return http.build()
    }
}

class CreateAccountFromJwtFilter(
    private val accountRepository: AccountRepository,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain
    ) {
        try {
            val jwt = SecurityContextHolder.getContext().authentication.principal as? Jwt
            if (jwt != null && !jwt.subject.isNullOrBlank() && accountRepository.findById(jwt.subject).isEmpty) {
                // TODO update existing app record fields (email, username, first_name, last_name) from incoming jwt data
                accountRepository.save(AccountEntity.fromDomain(jwt.toAccount()))
            }
        } finally {
            filterChain.doFilter(request, response)
        }
    }
}
