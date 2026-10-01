package com.duoc.bff_web.client;

import com.duoc.bff_web.dto.central.MovimientoCentralDto;
import com.duoc.bff_web.exception.AccesoDenegadoException;
import com.duoc.bff_web.exception.ServicioNoDisponibleException;
import com.duoc.bff_web.exception.TokenInvalidoException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Llamadas HTTP hacia banco-central-cuentas relacionadas a movimientos anuales.
 * No transforma datos, solo ejecuta la petición y devuelve el resultado.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MovimientoClient {

    private final RestClient bancoCentralRestClient;

    @Retry(name = "bancoCentral")
    @CircuitBreaker(name = "bancoCentral", fallbackMethod = "fallbackServicioNoDisponible")
    public List<MovimientoCentralDto> listarMovimientos(String token) {
        log.info("Llamando a banco-central-cuentas para movimientos recientes");
        try {
            return bancoCentralRestClient.get()
                    .uri("/api/movimientos/recientes")
                    .header("Authorization", token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<MovimientoCentralDto>>() {});
        } catch (HttpClientErrorException.Unauthorized ex) {
            throw new TokenInvalidoException();
        } catch (HttpClientErrorException.Forbidden ex) {
            throw new AccesoDenegadoException();
        } catch (ResourceAccessException ex) {
            throw new ServicioNoDisponibleException();
        }
    }

    private List<MovimientoCentralDto> fallbackServicioNoDisponible(
            String token, ServicioNoDisponibleException t) {
        log.warn("FALLBACK (cuentas no disponible) en listarMovimientos");
        throw new ServicioNoDisponibleException();
    }

    private List<MovimientoCentralDto> fallbackServicioNoDisponible(
            String token, CallNotPermittedException t) {
        log.warn("FALLBACK (circuito abierto) en listarMovimientos");
        throw new ServicioNoDisponibleException();
    }
}