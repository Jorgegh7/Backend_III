package com.duoc.bff_cajeros.controller;

import com.duoc.bff_cajeros.dto.RetiroRequestDto;
import com.duoc.bff_cajeros.dto.RetiroSolicitudResponseDto;
import com.duoc.bff_cajeros.dto.SaldoResponseDto;
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

    @GetMapping("/{id}/saldo")
    public SaldoResponseDto obtenerSaldo(@PathVariable Long id,
                                         @RequestHeader("Authorization") String token) {
        return cajeroService.consultarSaldo(id, token);
    }

    @PostMapping("/{id}/retiros")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RetiroSolicitudResponseDto solicitarRetiro(@PathVariable Long id,
                                                      @RequestBody RetiroRequestDto request) {
        return cajeroService.solicitarRetiro(id, request);
    }

    @GetMapping("/retiros/{solicitudId}")
    public RetiroResultadoEvent consultarEstadoRetiro(@PathVariable String solicitudId) {
        return retiroEstadoStore.obtener(solicitudId);
    }
}