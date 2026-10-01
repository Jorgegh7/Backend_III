package com.duoc.banco_central_cuentas.repository;

import com.duoc.banco_central_cuentas.entity.CuentaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CuentaBancariaRepository extends JpaRepository<CuentaBancaria, Long> {

    Optional<CuentaBancaria> findByCuentaIdLegacy(Long cuentaIdLegacy);
}
