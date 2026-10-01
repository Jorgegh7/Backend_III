package com.duoc.banco_central_xyz.controller;

import com.duoc.banco_central_xyz.dto.CuentaBancariaSaldoDto;
import com.duoc.banco_central_xyz.service.CuentaBancariaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OAuth2CuentaBancariaController {

    private final CuentaBancariaService cuentaBancariaService;

    @GetMapping("/api/oauth2/cuentas/{id}/saldo")
    public CuentaBancariaSaldoDto obtenerSaldoOAuth2(@PathVariable Long id) {
        return cuentaBancariaService.obtenerSaldoParaServicio(id);
    }
}