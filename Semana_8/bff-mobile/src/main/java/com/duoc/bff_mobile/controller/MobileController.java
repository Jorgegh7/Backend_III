package com.duoc.bff_mobile.controller;

import com.duoc.bff_mobile.client.CuentaBancariaOAuth2Client;
import com.duoc.bff_mobile.dto.SaldoResponseDto;
import com.duoc.bff_mobile.dto.central.SaldoOAuth2CentralDto;
import com.duoc.bff_mobile.service.MobileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bff-mobile/cuentas")
@RequiredArgsConstructor
public class MobileController {

    private final MobileService mobileService;
    private final CuentaBancariaOAuth2Client cuentaBancariaOAuth2Client;

    @GetMapping("/{id}/saldo")
    public SaldoResponseDto obtenerSaldo(@PathVariable Long id,
                                         @RequestHeader("Authorization") String token) {
        return mobileService.consultarSaldo(id, token);
    }

    @GetMapping("/{id}/saldo-oauth2")
    public SaldoOAuth2CentralDto obtenerSaldoOAuth2(@PathVariable Long id) {
        return cuentaBancariaOAuth2Client.obtenerSaldoConOAuth2(id);
    }

}