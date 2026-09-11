package com.duoc.bff_mobile.client;

import com.duoc.bff_mobile.dto.central.CuentaBancariaMobileCentralDto;
import com.duoc.bff_mobile.exception.AccesoDenegadoException;
import com.duoc.bff_mobile.exception.CuentaNoEncontradaException;
import com.duoc.bff_mobile.exception.ServicioNoDisponibleException;
import com.duoc.bff_mobile.exception.TokenInvalidoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Encapsula las llamadas HTTP hacia el Backend Central relacionadas a cuentas
 * bancarias. No transforma datos ni aplica lógica de negocio, solo ejecuta
 * la petición y devuelve el DTO tal como lo entrega el Backend Central.
 */
@Component
@RequiredArgsConstructor
public class CuentaBancariaClient {

    private final RestClient bancoCentralRestClient;

    public CuentaBancariaMobileCentralDto obtenerCuentaResumen(Long id, String token) {
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
}