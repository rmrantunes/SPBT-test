package co.bondspot.spbttest.springweb.configuration

import co.bondspot.spbttest.domain.contract.IEventPublisher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class EventConfig {
    @Autowired private lateinit var applicationEventPublisher: ApplicationEventPublisher

    @Bean
    fun getEventPublisher(): IEventPublisher =
        object : IEventPublisher {
            override fun publishEvent(e: Any) {
                applicationEventPublisher.publishEvent(e)
            }
        }
}
