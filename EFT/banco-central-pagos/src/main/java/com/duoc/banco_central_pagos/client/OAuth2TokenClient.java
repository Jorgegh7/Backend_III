package com.duoc.banco_central_pagos.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class OAuth2TokenClient {

    private final RestClient authRestClient;
    private final String clientId;
    private final String clientSecret;

    public OAuth2TokenClient(@Qualifier("authRestClient") RestClient authRestClient,
                             @Value("${auth.server.client-id}") String clientId,
                             @Value("${auth.server.client-secret}") String clientSecret) {
        this.authRestClient = authRestClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    // Pagos modifica saldos en Cuentas, por eso pide el scope de escritura.
    public String obtenerToken() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("scope", "cuentas.write");

        Map<String, Object> respuesta = authRestClient.post()
                .uri("/oauth2/token")
                .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        return (String) respuesta.get("access_token");
    }
}