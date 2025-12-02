package co.bondspot.spbttest.springweb.configuration

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info("My API", version = "v1"),
)
@SecurityScheme(
    name = "bearerJwt",
    bearerFormat = "JWT",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
)
//@SecurityScheme(
//    name = "apiKey",
//    type = SecuritySchemeType.APIKEY,
//)
class OpenApiConfiguration