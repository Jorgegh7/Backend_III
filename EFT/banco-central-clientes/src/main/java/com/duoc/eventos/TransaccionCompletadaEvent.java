package com.duoc.eventos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento publicado por banco-central-pagos cuando una transaccion termina
 * con exito. Lo consumen otros servicios (por ejemplo, Clientes para
 * notificar al titular de la cuenta).
 */
public record TransaccionCompletadaEvent(
        String solicitudId,
        Long cuentaId,
        String tipoTransaccion,
        BigDecimal monto,
        BigDecimal saldoFinal,
        LocalDateTime fechaTransaccion
) {}