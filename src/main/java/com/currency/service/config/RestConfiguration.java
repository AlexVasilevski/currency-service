package com.currency.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfiguration {

    private final String fixerIoServiceUrl;

    public RestConfiguration(@Value("${service.fixer-io-url}") String fixerIoServiceUrl) {
        this.fixerIoServiceUrl = fixerIoServiceUrl;
    }

    @Bean
    public RestTemplate fixerRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .rootUri(fixerIoServiceUrl)
                .build();
    }
}
