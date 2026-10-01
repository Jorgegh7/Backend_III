package com.duoc.bff_web.controller;

import com.duoc.bff_web.client.CuentaBancariaOAuth2Client;
import com.duoc.bff_web.dto.CuentaBancariaResponseDto;
import com.duoc.bff_web.dto.DashboardDto;
import com.duoc.bff_web.dto.central.SaldoOAuth2CentralDto;
import com.duoc.bff_web.service.DashboardService;
import com.duoc.bff_web.service.WebService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bff-web/cuentas")
@RequiredArgsConstructor
public class WebController {

    private final WebService webService;
    private final DashboardService dashboardService;
    private final CuentaBancariaOAuth2Client cuentaBancariaOAuth2Client;

    @GetMapping("/{id}")
    public CuentaBancariaResponseDto obtenerCuenta(@PathVariable Long id,
                                                   @RequestHeader("Authorization") String token) {
        return webService.consultarCuenta(id, token);
    }

    @GetMapping("/{id}/dashboard")
    public DashboardDto obtenerDashboard(@PathVariable Long id,
                                         @RequestHeader("Authorization") String token) {
        return dashboardService.obtenerDashboard(id, token);
    }

    @GetMapping("/{id}/saldo-oauth2")
    public SaldoOAuth2CentralDto obtenerSaldoOAuth2(@PathVariable Long id) {
        return cuentaBancariaOAuth2Client.obtenerSaldoConOAuth2(id);
    }
}