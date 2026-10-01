package com.duoc.eventos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RetiroSolicitadoEvent(
        String solicitudId,
        Long cuentaId,
        BigDecimal monto,
        LocalDateTime fechaSolicitud
) {}