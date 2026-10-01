package com.duoc.banco_central_cuentas.controller;

import com.duoc.banco_central_cuentas.dto.CuentaBancariaDto;
import com.duoc.banco_central_cuentas.dto.CuentaBancariaMobileDto;
import com.duoc.banco_central_cuentas.dto.CuentaBancariaSaldoDto;
import com.duoc.banco_central_cuentas.security.UsuarioAutenticado;
import com.duoc.banco_central_cuentas.service.CuentaBancariaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public CuentaBancariaDto obtenerPorId(@PathVariable Long id,
                                          @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return cuentaBancariaService.obtenerPorId(id, usuario);
    }

    @GetMapping("/{id}/saldo")
    public CuentaBancariaSaldoDto obtenerSaldo(@PathVariable Long id,
                                               @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return cuentaBancariaService.obtenerSaldo(id, usuario);
    }

    @GetMapping("/{id}/mobile")
    public CuentaBancariaMobileDto obtenerParaMobile(@PathVariable Long id,
                                                     @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return cuentaBancariaService.obtenerParaMobile(id, usuario);
    }
}