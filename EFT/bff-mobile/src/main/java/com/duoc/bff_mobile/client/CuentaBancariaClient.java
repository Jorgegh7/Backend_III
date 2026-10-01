package com.duoc.bff_mobile.client;

import com.duoc.bff_mobile.dto.central.CuentaBancariaMobileCentralDto;
import com.duoc.bff_mobile.exception.AccesoDenegadoException;
import com.duoc.bff_mobile.exception.CuentaNoEncontradaException;
import com.duoc.bff_mobile.exception.ServicioNoDisponibleException;
import com.duoc.bff_mobile.exception.TokenInvalidoException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Encapsula las llamadas HTTP hacia banco-central-cuentas relacionadas a cuentas
 * bancarias. No transforma datos ni aplica lógica de negocio, solo ejecuta
 * la petición y devuelve el DTO tal como lo entrega el servicio.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CuentaBancariaClient {

    private final RestClient bancoCentralRestClient;

    @Retry(name = "bancoCentral")
    @CircuitBreaker(name = "bancoCentral", fallbackMethod = "fallbackServicioNoDisponible")
    public CuentaBancariaMobileCentralDto obtenerCuentaResumen(Long id, String token) {
        log.info("Llamando a banco-central-cuentas para cuenta id={}", id);
        return ejecutarLlamada(id, token);
    }

    @RateLimiter(name = "bancoCentral")
    public CuentaBancariaMobileCentralDto obtenerCuentaResumenConLimite(Long id, String token) {
        log.info("Llamando a banco-central-cuentas (con rate limiter) para cuenta id={}", id);
        return ejecutarLlamada(id, token);
    }

    private CuentaBancariaMobileCentralDto ejecutarLlamada(Long id, String token) {
        try {
            return bancoCentralRestClient.get()
                    .uri("/api/cuentas/{id}/mobile", id)
                    .header("Authorization", token)
                    .retrieve()
                    .body(CuentaBancariaMobileCentralDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new CuentaNoEncontradaException(id);
        } catch (HttpClientErrorException.Unauthorized ex) {
            throw new TokenInvalidoException();
        } catch (HttpClientErrorException.Forbidden ex) {
            throw new AccesoDenegadoException();
        } catch (ResourceAccessException ex) {
            throw new ServicioNoDisponibleException();
        }
    }

    // Fallbacks tipados: solo se activan para "servicio caído" y "circuito abierto".
    // Los errores de negocio (403, 404, 401) no coinciden y se propagan tal cual.
    private CuentaBancariaMobileCentralDto fallbackServicioNoDisponible(
            Long id, String token, ServicioNoDisponibleException t) {
        log.warn("FALLBACK (servicio no disponible) para cuenta id={}", id);
        throw new ServicioNoDisponibleException();
    }

    private CuentaBancariaMobileCentralDto fallbackCircuitoAbierto(
            Long id, String token, CallNotPermittedException t) {
        log.warn("FALLBACK (circuito abierto) para cuenta id={}", id);
        throw new ServicioNoDisponibleException();
    }
}