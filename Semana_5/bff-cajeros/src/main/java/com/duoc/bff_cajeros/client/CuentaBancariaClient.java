package com.duoc.bff_cajeros.client;

import com.duoc.bff_cajeros.dto.central.SaldoCentralDto;
import com.duoc.bff_cajeros.exception.AccesoDenegadoException;
import com.duoc.bff_cajeros.exception.CuentaNoEncontradaException;
import com.duoc.bff_cajeros.exception.ServicioNoDisponibleException;
import com.duoc.bff_cajeros.exception.TokenInvalidoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class CuentaBancariaClient {

    private final RestClient bancoCentralRestClient;

    public SaldoCentralDto obtenerSaldo(Long id, String token) {
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
}