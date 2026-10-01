package com.duoc.banco_central_cuentas.controller;

import com.duoc.banco_central_cuentas.dto.CuentaBancariaSaldoDto;
import com.duoc.banco_central_cuentas.dto.RetiroCuentaBancariaRequestDto;
import com.duoc.banco_central_cuentas.dto.RetiroCuentaBancariaResponseDto;
import com.duoc.banco_central_cuentas.service.CuentaBancariaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuth2CuentaBancariaController {

    private final CuentaBancariaService cuentaBancariaService;

    public OAuth2CuentaBancariaController(CuentaBancariaService cuentaBancariaService) {
        this.cuentaBancariaService = cuentaBancariaService;
    }

    // Consulta de saldo servicio a servicio: exige el scope cuentas.read.
    @GetMapping("/api/oauth2/cuentas/{id}/saldo")
    public CuentaBancariaSaldoDto obtenerSaldoOAuth2(@PathVariable Long id) {
        return cuentaBancariaService.obtenerSaldoParaServicio(id);
    }

    // Descuento de saldo servicio a servicio (lo invoca Pagos): exige el scope cuentas.write.
    @PostMapping("/api/oauth2/cuentas/{id}/retiro")
    public RetiroCuentaBancariaResponseDto retirar(@PathVariable Long id,
                                                   @RequestBody RetiroCuentaBancariaRequestDto request) {
        return cuentaBancariaService.retirar(id, request);
    }
}