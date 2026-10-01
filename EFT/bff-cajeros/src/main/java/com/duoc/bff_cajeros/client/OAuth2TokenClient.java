package com.duoc.bff_cajeros.client;

import com.duoc.bff_cajeros.dto.central.TokenResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class OAuth2TokenClient {

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;

    public OAuth2TokenClient(
            @Value("${auth.server.url}") String authServerUrl,
            @Value("${auth.server.client-id}") String clientId,
            @Value("${auth.server.client-secret}") String clientSecret) {
        this.restClient = RestClient.builder().baseUrl(authServerUrl).build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String obtenerToken() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("scope", "cuentas.read");

        TokenResponseDto respuesta = restClient.post()
                .uri("/oauth2/token")
                .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(TokenResponseDto.class);

        log.info("Token OAuth2 obtenido, expira en {} segundos", respuesta.expires_in());
        return respuesta.access_token();
    }
}