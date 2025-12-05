package co.bondspot.spbttest.springweb.configuration

import java.util.Optional
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
class AuditConfig {
    @Bean
    fun auditorProvider(): AuditorAware<String> {
        return CustomAuditorAware()
    }
}

class CustomAuditorAware : AuditorAware<String> {
    override fun getCurrentAuditor(): Optional<String> {
        val authentication = SecurityContextHolder.getContext()?.authentication

        if (authentication == null || !authentication.isAuthenticated) {
            return Optional.empty()
        }

        val jwt = authentication.principal as? Jwt ?: return Optional.empty()

        return Optional.of(jwt.subject)
    }
}
