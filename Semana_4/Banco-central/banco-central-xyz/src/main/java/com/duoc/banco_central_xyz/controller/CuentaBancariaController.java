package com.duoc.banco_central_xyz.controller;

import com.duoc.banco_central_xyz.dto.CuentaBancariaDto;
import com.duoc.banco_central_xyz.dto.CuentaBancariaSaldoDto;
import com.duoc.banco_central_xyz.service.CuentaBancariaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaBancariaController {

    private final CuentaBancariaService cuentaBancariaService;

    public CuentaBancariaController(CuentaBancariaService cuentaBancariaService) {
        this.cuentaBancariaService = cuentaBancariaService;
    }

    @GetMapping
    @PreAuthorize("hasRole('EMPLEADO')")
    public List<CuentaBancariaDto> listarTodas() {
        return cuentaBancariaService.listarTodas();
    }

    @GetMapping("/{id}")
    public CuentaBancariaDto obtenerPorId(@PathVariable Long id, Authentication authentication) {
        return cuentaBancariaService.obtenerPorId(id, authentication.getName());
    }

    @GetMapping("/{id}/saldo")
    public CuentaBancariaSaldoDto obtenerSaldo(@PathVariable Long id, Authentication authentication) {
        return cuentaBancariaService.obtenerSaldo(id, authentication.getName());
    }
}