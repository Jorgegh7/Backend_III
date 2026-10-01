package com.duoc.bff_web.client;

import com.duoc.bff_web.dto.central.TransaccionCentralDto;
import com.duoc.bff_web.exception.AccesoDenegadoException;
import com.duoc.bff_web.exception.ServicioNoDisponibleException;
import com.duoc.bff_web.exception.TokenInvalidoException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Llamadas HTTP hacia banco-central-pagos (transacciones). Usa su propio
 * RestClient y su propia instancia de Resilience4j ("pagos"), para que una
 * caída de Pagos no afecte el circuito de Cuentas.
 */
@Slf4j
@Component
public class TransaccionClient {

    private final RestClient pagosRestClient;

    public TransaccionClient(@Qualifier("pagosRestClient") RestClient pagosRestClient) {
        this.pagosRestClient = pagosRestClient;
    }

    @Retry(name = "pagos")
    @CircuitBreaker(name = "pagos", fallbackMethod = "fallbackServicioNoDisponible")
    public List<TransaccionCentralDto> listarTransacciones(String token) {
        log.info("Llamando a banco-central-pagos para transacciones recientes");
        try {
            return pagosRestClient.get()
                    .uri("/api/transacciones/recientes")
                    .header("Authorization", token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<TransaccionCentralDto>>() {});
        } catch (HttpClientErrorException.Unauthorized ex) {
            throw new TokenInvalidoException();
        } catch (HttpClientErrorException.Forbidden ex) {
            throw new AccesoDenegadoException();
        } catch (ResourceAccessException ex) {
            throw new ServicioNoDisponibleException();
        }
    }

    private List<TransaccionCentralDto> fallbackServicioNoDisponible(
            String token, ServicioNoDisponibleException t) {
        log.warn("FALLBACK (pagos no disponible) en listarTransacciones");
        throw new ServicioNoDisponibleException();
    }

    private List<TransaccionCentralDto> fallbackServicioNoDisponible(
            String token, CallNotPermittedException t) {
        log.warn("FALLBACK (circuito de pagos abierto) en listarTransacciones");
        throw new ServicioNoDisponibleException();
    }
}