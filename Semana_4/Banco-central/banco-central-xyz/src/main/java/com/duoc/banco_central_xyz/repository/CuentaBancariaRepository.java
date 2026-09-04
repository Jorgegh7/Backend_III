package com.duoc.banco_central_xyz.repository;

import com.duoc.banco_central_xyz.entity.CuentaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CuentaBancariaRepository extends JpaRepository<CuentaBancaria, Long> {

    Optional<CuentaBancaria> findByCuentaIdLegacy(Long cuentaIdLegacy);
}
