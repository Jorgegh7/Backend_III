package com.duoc.banco_central_xyz.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransaccionDto(
        Long idLegacy,
        LocalDate fecha,
        BigDecimal monto,
        String tipo
) {
}
