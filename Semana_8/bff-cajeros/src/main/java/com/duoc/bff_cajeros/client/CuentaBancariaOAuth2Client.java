package com.duoc.bff_cajeros.client;

import com.duoc.bff_cajeros.dto.central.SaldoOAuth2CentralDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class CuentaBancariaOAuth2Client {

    private final RestClient bancoCentralRestClient;
    private final OAuth2TokenClient tokenClient;

    public SaldoOAuth2CentralDto obtenerSaldoConOAuth2(Long id) {
        String token = tokenClient.obtenerToken();

        log.info("Llamando a banco-central-xyz (OAuth2) para cuenta id={}", id);

        return bancoCentralRestClient.get()
                .uri("/api/oauth2/cuentas/{id}/saldo", id)
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .body(SaldoOAuth2CentralDto.class);
    }
}