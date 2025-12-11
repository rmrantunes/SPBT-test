package co.bondspot.spbttest.springweb.configuration

import co.bondspot.spbttest.domain.contract.IFgaProvider
import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import co.bondspot.spbttest.domain.contract.IIAMProvider
import co.bondspot.spbttest.infrastructure.fga.OpenFgaProvider
import co.bondspot.spbttest.infrastructure.iam.KeycloakIAMProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ProviderBeanConfig {
    @Bean fun getIAMProvider(): IIAMProvider = KeycloakIAMProvider()

    @Bean fun getFgaProvider(): IFgaProvider = OpenFgaProvider()

    @Bean
    fun getFtsProvider(): IFullTextSearchProvider =
        object : IFullTextSearchProvider {
            override fun <T> index(collection: String, items: List<T>) {
                TODO("Not yet implemented")
            }

            override fun <T> search(
                collection: String,
                query: String,
                ids: List<String>?,
            ): List<T> {
                TODO("Not yet implemented")
            }
        }
}
