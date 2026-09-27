package com.duoc.bff_web.dto.central;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimientoCentralDto(
        Long cuentaIdLegacy,
        LocalDate fecha,
        String transaccion,
        BigDecimal monto,
        String descripcion
) {}