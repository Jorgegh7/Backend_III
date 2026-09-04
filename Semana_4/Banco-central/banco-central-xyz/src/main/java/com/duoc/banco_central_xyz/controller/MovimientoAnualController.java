package com.duoc.banco_central_xyz.controller;

import com.duoc.banco_central_xyz.dto.MovimientoAnualDto;
import com.duoc.banco_central_xyz.service.MovimientoAnualService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoAnualController {

    private final MovimientoAnualService movimientoAnualService;

    public MovimientoAnualController(MovimientoAnualService movimientoAnualService) {
        this.movimientoAnualService = movimientoAnualService;
    }

    @GetMapping
    @PreAuthorize("hasRole('EMPLEADO')")
    public List<MovimientoAnualDto> listarMovimientosAnuales(){
        return movimientoAnualService.listarMovimientosAnuales();
    }

    @GetMapping("/{id}")
    public MovimientoAnualDto obtenerPorId(@PathVariable Long id) {
        return movimientoAnualService.obtenerPorId(id);
    }

}
