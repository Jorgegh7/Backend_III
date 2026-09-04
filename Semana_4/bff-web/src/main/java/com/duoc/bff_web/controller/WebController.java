package com.duoc.bff_web.controller;

import com.duoc.bff_web.dto.CuentaBancariaResponseDto;
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

    @GetMapping("/{id}")
    public CuentaBancariaResponseDto obtenerCuenta(@PathVariable Long id,
                                                   @RequestHeader("Authorization") String token) {
        return webService.consultarCuenta(id, token);
    }
}