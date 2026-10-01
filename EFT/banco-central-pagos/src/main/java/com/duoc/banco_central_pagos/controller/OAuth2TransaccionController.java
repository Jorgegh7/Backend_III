package com.duoc.banco_central_pagos.controller;

import com.duoc.banco_central_pagos.dto.TransaccionDto;
import com.duoc.banco_central_pagos.service.TransaccionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OAuth2TransaccionController {

    private final TransaccionService transaccionService;

    public OAuth2TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    // Consulta servicio a servicio: transacciones recientes. Exige el scope pagos.read.
    @GetMapping("/api/oauth2/transacciones/recientes")
    public List<TransaccionDto> listarRecientesOAuth2() {
        return transaccionService.listarRecientes();
    }
}