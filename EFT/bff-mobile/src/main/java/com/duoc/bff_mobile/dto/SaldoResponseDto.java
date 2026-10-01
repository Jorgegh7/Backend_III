package com.duoc.bff_mobile.dto;

import java.math.BigDecimal;

public record SaldoResponseDto(
        String nombre,
        BigDecimal saldo,
        String tipoCuenta
) {
}
