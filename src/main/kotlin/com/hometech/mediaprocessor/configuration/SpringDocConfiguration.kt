package com.hometech.mediaprocessor.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.core.jackson.ModelResolver
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.OpenApiCustomiser
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpringDocConfiguration(val appProperties: AppProperties) {

    init {
        ModelResolver.enumsAsRef = true
    }

    @Bean
    fun modelResolver(objectMapper: ObjectMapper): ModelResolver {
        return ModelResolver(objectMapper)
    }

    @Bean
    fun openAPI(): OpenAPI {
        val info = Info()
            .title("mediaprocessor-service")
        val server = Server().apply { this.url = appProperties.baseUrl }
        return OpenAPI()
            .servers(listOf(server))
            .info(info)
    }

    @Bean
    fun openApiCustomiser(): OpenApiCustomiser = OpenApiCustomiser { openApi ->
        openApi.paths.forEach { path ->
            if (path.key.startsWith("/api")) {
                path.value.readOperations().forEach { operation ->
                    operation.security = listOf()
                }
            }
        }
    }
}
