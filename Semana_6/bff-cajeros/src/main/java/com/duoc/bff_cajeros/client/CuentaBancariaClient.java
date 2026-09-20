package com.duoc.bff_cajeros.client;

import com.duoc.bff_cajeros.dto.central.SaldoCentralDto;
import com.duoc.bff_cajeros.exception.AccesoDenegadoException;
import com.duoc.bff_cajeros.exception.CuentaNoEncontradaException;
import com.duoc.bff_cajeros.exception.ServicioNoDisponibleException;
import com.duoc.bff_cajeros.exception.TokenInvalidoException;
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
     * Llamada con Circuit Breaker + Retry:
     * - CircuitBreaker: abre el circuito tras 50% de fallos en ventana de 10 llamadas
     * - Retry: reintenta hasta 3 veces con delay exponencial (1s, 2s)
     */
    @Retry(name = "bancoCentral")
    @CircuitBreaker(name = "bancoCentral", fallbackMethod = "obtenerSaldoFallback")
    public SaldoCentralDto obtenerSaldo(Long id, String token) {
        log.info("Llamando a banco-central-xyz para cuenta id={}", id);
        return ejecutarLlamada(id, token);
    }

    /**
     * Llamada con Rate Limiter: limita el numero de llamadas permitidas en un
     * periodo de tiempo, independiente de si el servicio falla o no.
     */
    @RateLimiter(name = "bancoCentral")
    public SaldoCentralDto obtenerSaldoConLimite(Long id, String token) {
        log.info("Llamando a banco-central-xyz (con rate limiter) para cuenta id={}", id);
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

    private SaldoCentralDto obtenerSaldoFallback(Long id, String token, Throwable t) {
        log.warn("FALLBACK activado para obtenerSaldo(id={}). Causa: {}", id, t.getMessage());
        throw new ServicioNoDisponibleException();
    }
}