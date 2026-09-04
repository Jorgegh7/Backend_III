package com.duoc.banco_central_xyz.service;

import com.duoc.banco_central_xyz.dto.TransaccionDto;
import com.duoc.banco_central_xyz.entity.Transaccion;
import com.duoc.banco_central_xyz.exception.TransaccionNoEncontradaException;
import com.duoc.banco_central_xyz.mapper.TransaccionMapper;
import com.duoc.banco_central_xyz.repository.TransaccionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final TransaccionMapper transaccionMapper;

    public TransaccionService(TransaccionRepository transaccionRepository, TransaccionMapper transaccionMapper) {
        this.transaccionRepository = transaccionRepository;
        this.transaccionMapper = transaccionMapper;
    }

    public List<TransaccionDto> listarTransacciones(){
        List<Transaccion> entidades = transaccionRepository.findAll();

        return entidades.stream()
                .map(transaccionMapper::toDto)
                .toList();
    }

    public List<TransaccionDto> listarDebito(){
        List<Transaccion> entidades = transaccionRepository.findByTipo("debito");

        return entidades.stream()
                .map(transaccionMapper::toDto)
                .toList();
    }

    public List<TransaccionDto> listarCredito(){
        List<Transaccion> entidades = transaccionRepository.findByTipo("credito");

        return entidades.stream()
                .map(transaccionMapper::toDto)
                .toList();
    }

    public TransaccionDto obtenerPorId(Long id) {
        return transaccionRepository.findById(id)
                .map(transaccionMapper::toDto)
                .orElseThrow(() -> new TransaccionNoEncontradaException(id));
    }


}
