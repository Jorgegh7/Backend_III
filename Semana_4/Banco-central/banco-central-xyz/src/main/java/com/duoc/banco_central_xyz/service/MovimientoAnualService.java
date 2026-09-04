package com.duoc.banco_central_xyz.service;

import com.duoc.banco_central_xyz.dto.MovimientoAnualDto;
import com.duoc.banco_central_xyz.exception.MovimientoNoEncontradoException;
import com.duoc.banco_central_xyz.mapper.MovimientoAnualMapper;
import com.duoc.banco_central_xyz.repository.MovimientoAnualRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovimientoAnualService {

    private final MovimientoAnualRepository movimientoAnualRepository;
    private final MovimientoAnualMapper movimientoAnualMapper;

    public MovimientoAnualService(MovimientoAnualRepository movimientoAnualRepository, MovimientoAnualMapper movimientoAnualMapper) {
        this.movimientoAnualRepository = movimientoAnualRepository;
        this.movimientoAnualMapper = movimientoAnualMapper;
    }

    public List<MovimientoAnualDto> listarMovimientosAnuales(){
        return movimientoAnualRepository.findAll()
                .stream()
                .map(movimientoAnualMapper::toDto)
                .toList();
    }

    public MovimientoAnualDto obtenerPorId(Long id) {
        return movimientoAnualRepository.findById(id)
                .map(movimientoAnualMapper::toDto)
                .orElseThrow(() -> new MovimientoNoEncontradoException(id));
    }

}
