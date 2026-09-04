package com.duoc.banco_central_xyz.controller;

import com.duoc.banco_central_xyz.dto.TransaccionDto;
import com.duoc.banco_central_xyz.service.TransaccionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @GetMapping
    @PreAuthorize("hasRole('EMPLEADO')")
    public List<TransaccionDto> listarTransacciones(){
        return transaccionService.listarTransacciones();
    }

    @GetMapping("/credito")
    public List<TransaccionDto> listarCredito(){
        return transaccionService.listarCredito();
    }

    @GetMapping("/debito")
    public List<TransaccionDto> listarDebito(){
        return transaccionService.listarDebito();
    }

    @GetMapping("/{id}")
    public TransaccionDto obtenerPorId(@PathVariable Long id){
        return transaccionService.obtenerPorId(id);
    }



}
