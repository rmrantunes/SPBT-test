package co.bondspot.spbttest.springweb.configuration

import co.bondspot.spbttest.domain.contract.IAMProviderContract
import co.bondspot.spbttest.infrastructure.iam.KeycloakIAMProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ProviderBeanConfiguration {
    @Bean
    fun getIAMProvider(): IAMProviderContract = KeycloakIAMProvider()
}