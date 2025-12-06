package co.bondspot.spbttest.springweb.configuration

import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.KotlinSerializationJsonHttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    @Bean
    fun kotlinSerializationConverter(): KotlinSerializationJsonHttpMessageConverter {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        return KotlinSerializationJsonHttpMessageConverter(json)
    }
}
