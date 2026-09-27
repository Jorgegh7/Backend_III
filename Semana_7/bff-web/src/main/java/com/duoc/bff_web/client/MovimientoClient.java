package com.duoc.bff_web.client;

import com.duoc.bff_web.dto.central.MovimientoCentralDto;
import com.duoc.bff_web.exception.AccesoDenegadoException;
import com.duoc.bff_web.exception.ServicioNoDisponibleException;
import com.duoc.bff_web.exception.TokenInvalidoException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Encapsula las llamadas HTTP hacia el Backend Central relacionadas a
 * movimientos anuales. No transforma datos, solo ejecuta la petición y
 * devuelve el resultado tal como lo entrega el Backend Central.
 */
@Component
@RequiredArgsConstructor
public class MovimientoClient {

    private final RestClient bancoCentralRestClient;

    public List<MovimientoCentralDto> listarMovimientos(String token) {
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
}