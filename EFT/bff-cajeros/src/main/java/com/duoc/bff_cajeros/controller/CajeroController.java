package com.duoc.bff_cajeros.controller;

import com.duoc.bff_cajeros.client.CuentaBancariaOAuth2Client;
import com.duoc.bff_cajeros.dto.RetiroRequestDto;
import com.duoc.bff_cajeros.dto.RetiroSolicitudResponseDto;
import com.duoc.bff_cajeros.dto.SaldoResponseDto;
import com.duoc.bff_cajeros.dto.central.SaldoOAuth2CentralDto;
import com.duoc.bff_cajeros.service.CajeroService;
import com.duoc.bff_cajeros.service.RetiroEstadoStore;
import com.duoc.eventos.RetiroResultadoEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bff-cajero/cuentas")
@RequiredArgsConstructor
public class CajeroController {

    private final CajeroService cajeroService;
    private final RetiroEstadoStore retiroEstadoStore;
    private final CuentaBancariaOAuth2Client cuentaBancariaOAuth2Client;

    @GetMapping("/{id}/saldo")
    public SaldoResponseDto obtenerSaldo(@PathVariable Long id,
                                         @RequestHeader("Authorization") String token) {
        return cajeroService.consultarSaldo(id, token);
    }

    @PostMapping("/{id}/retiros")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RetiroSolicitudResponseDto solicitarRetiro(@PathVariable Long id,
                                                      @RequestHeader("Authorization") String token,
                                                      @RequestBody RetiroRequestDto request) {
        return cajeroService.solicitarRetiro(id, request, token);
    }

    @GetMapping("/retiros/{solicitudId}")
    public RetiroResultadoEvent consultarEstadoRetiro(@PathVariable String solicitudId) {
        return retiroEstadoStore.obtener(solicitudId);
    }

    @GetMapping("/{id}/saldo-oauth2")
    public SaldoOAuth2CentralDto obtenerSaldoOAuth2(@PathVariable Long id) {
        return cuentaBancariaOAuth2Client.obtenerSaldoConOAuth2(id);
    }
}