package com.duoc.bff_cajeros.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Crea el RestClient usado para llamar al Backend Central,
 * con la URL base ya configurada.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient bancoCentralRestClient(@Value("${banco.central.url}") String bancoCentralUrl) {
        return RestClient.builder()
                .baseUrl(bancoCentralUrl)
                .build();
    }
}