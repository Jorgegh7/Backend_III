package com.duoc.bff_web.client;

import com.duoc.bff_web.dto.central.CuentaBancariaCentralDto;
import com.duoc.bff_web.exception.AccesoDenegadoException;
import com.duoc.bff_web.exception.CuentaNoEncontradaException;
import com.duoc.bff_web.exception.TokenInvalidoException;
import com.duoc.bff_web.exception.ServicioNoDisponibleException;
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
 * Encapsula las llamadas HTTP hacia el Backend Central relacionadas a cuentas
 * bancarias. No transforma datos ni aplica lógica de negocio — solo ejecuta
 * la petición y devuelve el DTO tal como lo entrega el Backend Central.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CuentaBancariaClient {

    private final RestClient bancoCentralRestClient;

    @Retry(name = "bancoCentral")
    @CircuitBreaker(name = "bancoCentral", fallbackMethod = "obtenerCuentaFallback")
    public CuentaBancariaCentralDto obtenerCuenta(Long id, String token) {
        log.info("Llamando a banco-central-xyz para cuenta id={}", id);
        return ejecutarLlamada(id, token);
    }

    @RateLimiter(name = "bancoCentral")
    public CuentaBancariaCentralDto obtenerCuentaConLimite(Long id, String token) {
        log.info("Llamando a banco-central-xyz (con rate limiter) para cuenta id={}", id);
        return ejecutarLlamada(id, token);
    }

    private CuentaBancariaCentralDto ejecutarLlamada(Long id, String token) {
        try {
            return bancoCentralRestClient.get()
                    .uri("/api/cuentas/{id}", id)
                    .header("Authorization", token)
                    .retrieve()
                    .body(CuentaBancariaCentralDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new CuentaNoEncontradaException(id);
        } catch (HttpClientErrorException.Unauthorized ex) {
            throw new TokenInvalidoException();
        } catch (ResourceAccessException ex) {
            throw new ServicioNoDisponibleException();
        } catch (HttpClientErrorException.Forbidden ex) {
            throw new AccesoDenegadoException();
        }
    }

    private CuentaBancariaCentralDto obtenerCuentaFallback(Long id, String token, Throwable t) {
        log.warn("FALLBACK activado para obtenerCuenta(id={}). Causa: {}", id, t.getMessage());
        throw new ServicioNoDisponibleException();
    }
}