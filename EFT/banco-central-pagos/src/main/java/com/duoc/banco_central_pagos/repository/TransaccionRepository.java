package com.duoc.banco_central_pagos.repository;

import com.duoc.banco_central_pagos.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByTipo(String tipo);
    List<Transaccion> findAllByOrderByFechaDesc();
}
