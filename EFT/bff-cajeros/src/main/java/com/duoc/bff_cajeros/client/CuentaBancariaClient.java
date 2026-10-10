package com.duoc.bff_cajeros.client;

import com.duoc.bff_cajeros.dto.central.SaldoCentralDto;
import com.duoc.bff_cajeros.exception.AccesoDenegadoException;
import com.duoc.bff_cajeros.exception.CuentaNoEncontradaException;
import com.duoc.bff_cajeros.exception.ServicioNoDisponibleException;
import com.duoc.bff_cajeros.exception.TokenInvalidoException;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class CuentaBancariaClient {

    private final RestClient bancoCentralRestClient;

    /**
     * Llamada con Retry, Circuit Breaker y Rate Limiter. Solo las fallas de
     * disponibilidad activan el fallback; los errores de negocio (403, 404, 401)
     * y el rechazo del rate limiter (429) se propagan tal cual al cliente.
     */
    @Retry(name = "bancoCentral")
    @CircuitBreaker(name = "bancoCentral", fallbackMethod = "obtenerSaldoFallback")
    @RateLimiter(name = "bancoCentral")
    public SaldoCentralDto obtenerSaldo(Long id, String token) {
        log.info("Llamando al servicio de cuentas para cuenta id={}", id);
        return ejecutarLlamada(id, token);
    }

    private SaldoCentralDto ejecutarLlamada(Long id, String token) {
        try {
            return bancoCentralRestClient.get()
                    .uri("/api/cuentas/{id}/saldo", id)
                    .header("Authorization", token)
                    .retrieve()
                    .body(SaldoCentralDto.class);
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

    // Fallback 1: el servicio de cuentas no responde.
    private SaldoCentralDto obtenerSaldoFallback(Long id, String token, ServicioNoDisponibleException t) {
        log.warn("FALLBACK activado para obtenerSaldo(id={}). Servicio de cuentas no disponible", id);
        throw new ServicioNoDisponibleException();
    }

    // Fallback 2: el circuito esta abierto y no deja pasar mas llamadas.
    private SaldoCentralDto obtenerSaldoFallback(Long id, String token, CallNotPermittedException t) {
        log.warn("FALLBACK activado para obtenerSaldo(id={}). Circuito abierto: {}", id, t.getMessage());
        throw new ServicioNoDisponibleException();
    }
}