package com.duoc.bff_web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Crea los RestClient de bff-web: uno hacia banco-central-cuentas
 * (cuentas y movimientos) y otro hacia banco-central-pagos (transacciones).
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient bancoCentralRestClient(@Value("${banco.central.url}") String bancoCentralUrl) {
        return RestClient.builder()
                .baseUrl(bancoCentralUrl)
                .build();
    }

    @Bean
    public RestClient pagosRestClient(@Value("${banco.pagos.url}") String pagosUrl) {
        return RestClient.builder()
                .baseUrl(pagosUrl)
                .build();
    }
}