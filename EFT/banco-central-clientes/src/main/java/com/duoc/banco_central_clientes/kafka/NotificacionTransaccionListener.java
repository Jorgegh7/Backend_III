package com.duoc.banco_central_clientes.kafka;

import com.duoc.eventos.TransaccionCompletadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consume las transacciones completadas que publica banco-central-pagos y
 * simula la notificacion al titular de la cuenta.
 */
@Component
public class NotificacionTransaccionListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacionTransaccionListener.class);

    @KafkaListener(
            topics = "transacciones-completadas",
            groupId = "banco-central-clientes-notificaciones",
            concurrency = "3"
    )
    public void notificar(TransaccionCompletadaEvent evento) {
        log.info("NOTIFICACION al titular de la cuenta {}: se realizo un {} por {} (saldo final: {}) - solicitudId={}",
                evento.cuentaId(),
                evento.tipoTransaccion(),
                evento.monto(),
                evento.saldoFinal(),
                evento.solicitudId());
    }
}