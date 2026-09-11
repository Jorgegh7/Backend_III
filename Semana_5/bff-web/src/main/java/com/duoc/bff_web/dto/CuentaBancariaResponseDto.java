package com.duoc.bff_web.dto;

import java.math.BigDecimal;

public record CuentaBancariaResponseDto(
        Long cuentaIdLegacy,
        String nombre,
        BigDecimal saldo,
        Integer edad,
        String tipo,
        BigDecimal interes,
        BigDecimal saldoFinal
) {
}
