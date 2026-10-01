package com.duoc.bff_mobile.dto.central;

import java.math.BigDecimal;

public record CuentaBancariaMobileCentralDto(
        String nombre,
        BigDecimal saldo,
        String tipo
) {
}
