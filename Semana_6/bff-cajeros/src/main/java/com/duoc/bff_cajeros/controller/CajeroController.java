package com.duoc.bff_cajeros.controller;

import com.duoc.bff_cajeros.dto.SaldoResponseDto;
import com.duoc.bff_cajeros.service.CajeroService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bff-cajero/cuentas")
@RequiredArgsConstructor
public class CajeroController {

    private final CajeroService cajeroService;

    @GetMapping("/{id}/saldo")
    public SaldoResponseDto obtenerSaldo(@PathVariable Long id,
                                         @RequestHeader("Authorization") String token) {
        return cajeroService.consultarSaldo(id, token);
    }
}