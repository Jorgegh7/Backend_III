package com.duoc.bff_mobile.dto.central;

import java.math.BigDecimal;

public record CuentaBancariaCentralDto(
        Long cuentaIdLegacy,
        String nombre,
        BigDecimal saldo,
        Integer edad,
        String tipo,
        BigDecimal interes,
        BigDecimal saldoFinal
) {
}
