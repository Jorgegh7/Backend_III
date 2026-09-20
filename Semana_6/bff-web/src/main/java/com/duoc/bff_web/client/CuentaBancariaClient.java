package com.duoc.bff_web.client;

import com.duoc.bff_web.dto.central.CuentaBancariaCentralDto;
import com.duoc.bff_web.exception.AccesoDenegadoException;
import com.duoc.bff_web.exception.CuentaNoEncontradaException;
import com.duoc.bff_web.exception.TokenInvalidoException;
import com.duoc.bff_web.exception.ServicioNoDisponibleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Encapsula las llamadas HTTP hacia el Backend Central relacionadas a cuentas
 * bancarias. No transforma datos ni aplica lógica de negocio — solo ejecuta
 * la petición y devuelve el DTO tal como lo entrega el Backend Central.
 */
@Component
@RequiredArgsConstructor
public class CuentaBancariaClient {

    private final RestClient bancoCentralRestClient;

    public CuentaBancariaCentralDto obtenerCuenta(Long id, String token) {
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
}