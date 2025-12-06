package co.bondspot.spbttest.springweb.configuration

import co.bondspot.spbttest.domain.contract.IIAMProvider
import co.bondspot.spbttest.infrastructure.iam.KeycloakIAMProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ProviderBeanConfig {
    @Bean fun getIAMProvider(): IIAMProvider = KeycloakIAMProvider()
}
