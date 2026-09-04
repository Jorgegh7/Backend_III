package com.duoc.banco_central_xyz.dto;

import java.math.BigDecimal;

public record CuentaBancariaDto(
        Long cuentaIdLegacy,
        String nombre,
        BigDecimal saldo,
        Integer edad,
        String tipo,
        BigDecimal interes,
        BigDecimal saldoFinal
) {
}
