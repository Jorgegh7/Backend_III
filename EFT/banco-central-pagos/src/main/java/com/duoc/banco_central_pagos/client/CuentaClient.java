package com.duoc.banco_central_pagos.client;

import com.duoc.banco_central_pagos.dto.ResultadoRetiro;
import com.duoc.banco_central_pagos.dto.RetiroCuentaBancariaRequestDto;
import com.duoc.banco_central_pagos.dto.RetiroCuentaBancariaResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
public class CuentaClient {

    private final RestClient cuentasRestClient;
    private final OAuth2TokenClient tokenClient;

    public CuentaClient(@Qualifier("cuentasRestClient") RestClient cuentasRestClient,
                        OAuth2TokenClient tokenClient) {
        this.cuentasRestClient = cuentasRestClient;
        this.tokenClient = tokenClient;
    }

    // Sin @Retry a proposito: el descuento no es idempotente y un reintento podria descontar dos veces.
    @CircuitBreaker(name = "cuentas", fallbackMethod = "retirarFallback")
    public ResultadoRetiro retirar(Long cuentaId, BigDecimal monto) {
        String token = tokenClient.obtenerToken();
        log.info("Solicitando a banco-central-cuentas el descuento de {} en la cuenta id={}", monto, cuentaId);

        try {
            RetiroCuentaBancariaResponseDto detalle = cuentasRestClient.post()
                    .uri("/api/oauth2/cuentas/{id}/retiro", cuentaId)
                    .headers(headers -> headers.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new RetiroCuentaBancariaRequestDto(monto))
                    .retrieve()
                    .body(RetiroCuentaBancariaResponseDto.class);
            return ResultadoRetiro.conExito(detalle);

        } catch (HttpClientErrorException.BadRequest | HttpClientErrorException.NotFound ex) {
            // Rechazo de negocio (fondos insuficientes, cuenta que no es de ahorro, cuenta inexistente):
            // no es una falla del servicio, por eso no cuenta para abrir el circuito.
            return ResultadoRetiro.conRechazo(extraerMensaje(ex));
        }
    }

    private ResultadoRetiro retirarFallback(Long cuentaId, BigDecimal monto, Throwable t) {
        log.warn("FALLBACK activado para el retiro de la cuenta id={}. Causa: {}", cuentaId, t.getMessage());
        return ResultadoRetiro.conRechazo("El servicio de cuentas no esta disponible. Intenta nuevamente mas tarde");
    }

    @SuppressWarnings("unchecked")
    private String extraerMensaje(HttpClientErrorException ex) {
        Map<String, Object> cuerpo = ex.getResponseBodyAs(Map.class);
        Object mensaje = cuerpo == null ? null : cuerpo.get("mensaje");
        return mensaje != null ? mensaje.toString() : "Retiro rechazado por el servicio de cuentas";
    }
}