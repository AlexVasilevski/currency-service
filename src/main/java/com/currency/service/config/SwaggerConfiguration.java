package com.currency.service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    private final String serviceContextPath;

    public SwaggerConfiguration(@Value("${server.servlet.context-path}") String serviceContextPath) {
        this.serviceContextPath = serviceContextPath;
    }

    @Bean
    public GroupedOpenApi groupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("Currency service API")
                .packagesToScan("com.currency.service")
                .build();
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().addServersItem(new Server().url(serviceContextPath))
                .info(
                        new Info().title("Currency service")
                                .description("Here you can find currency exchange rates")
                                .version("1.0.0")
                );

    }
}