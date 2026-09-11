package com.duoc.bff_web.client;

import com.duoc.bff_web.dto.central.TransaccionCentralDto;
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

@Component
@RequiredArgsConstructor
public class TransaccionClient {

    private final RestClient bancoCentralRestClient;

    public List<TransaccionCentralDto> listarTransacciones(String token) {
        try {
            return bancoCentralRestClient.get()
                    .uri("/api/transacciones/recientes")
                    .header("Authorization", token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<TransaccionCentralDto>>() {});
        } catch (HttpClientErrorException.Unauthorized ex) {
            throw new TokenInvalidoException();
        } catch (ResourceAccessException ex) {
            throw new ServicioNoDisponibleException();
        }
    }
}