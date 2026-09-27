package com.duoc.banco_central_xyz.dto;

import java.math.BigDecimal;

/**
 * Se construye el DTO campo por campo (no se reenvía el objeto del
 * Backend Central directamente) para mantener el control explícito
 * sobre qué se expone al canal web, incluso cuando hoy coincide con
 * el detalle completo. Si el Backend Central agrega campos internos
 * en el futuro, no se exponen automáticamente, hay que agregarlos
 * aquí deliberadamente.
 */
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
