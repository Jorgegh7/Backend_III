package com.duoc.banco_central_xyz.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimientoAnualDto(
        Long cuentaIdLegacy,
        LocalDate fecha,
        String transaccion,
        BigDecimal monto,
        String descripcion
) {}