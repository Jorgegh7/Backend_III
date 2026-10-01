package com.duoc.eventos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RetiroResultadoEvent(
        String solicitudId,
        Long cuentaId,
        String estado,
        BigDecimal monto,
        BigDecimal saldoInicial,
        BigDecimal saldoFinal,
        String motivo,
        LocalDateTime fechaProcesamiento
) {}