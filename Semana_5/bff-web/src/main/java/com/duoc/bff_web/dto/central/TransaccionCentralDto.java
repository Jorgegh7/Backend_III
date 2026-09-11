package com.duoc.bff_web.dto.central;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransaccionCentralDto(
        Long idLegacy,
        LocalDate fecha,
        BigDecimal monto,
        String tipo
) {}