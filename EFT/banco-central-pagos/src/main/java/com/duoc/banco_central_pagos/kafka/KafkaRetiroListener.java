package com.duoc.banco_central_pagos.kafka;

import com.duoc.banco_central_pagos.client.CuentaClient;
import com.duoc.banco_central_pagos.dto.ResultadoRetiro;
import com.duoc.banco_central_pagos.dto.RetiroCuentaBancariaResponseDto;
import com.duoc.eventos.RetiroResultadoEvent;
import com.duoc.eventos.RetiroSolicitadoEvent;
import com.duoc.eventos.TransaccionCompletadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaRetiroListener {

    private final CuentaClient cuentaClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_APROBADOS = "retiros-aprobados";
    private static final String TOPIC_RECHAZADOS = "retiros-rechazados";
    private static final String TOPIC_TRANSACCIONES_COMPLETADAS = "transacciones-completadas";

    @KafkaListener(
            topics = "retiros-solicitados",
            groupId = "banco-central-retiros",
            concurrency = "3"
    )
    public void procesarRetiro(RetiroSolicitadoEvent evento) {
        log.info("Consumido RetiroSolicitadoEvent: solicitudId={}, cuentaId={}, monto={}",
                evento.solicitudId(), evento.cuentaId(), evento.monto());

        ResultadoRetiro resultado = cuentaClient.retirar(evento.cuentaId(), evento.monto());

        if (resultado.aprobado()) {
            RetiroCuentaBancariaResponseDto detalle = resultado.detalle();
            RetiroResultadoEvent eventoResultado = new RetiroResultadoEvent(
                    evento.solicitudId(),
                    evento.cuentaId(),
                    "APROBADO",
                    evento.monto(),
                    detalle.saldoInicial(),
                    detalle.saldoFinal(),
                    resultado.motivo(),
                    LocalDateTime.now()
            );
            kafkaTemplate.send(TOPIC_APROBADOS, evento.cuentaId().toString(), eventoResultado);
            log.info("Publicado RetiroResultadoEvent APROBADO: solicitudId={}", evento.solicitudId());

            // Evento nuevo: la transaccion se completo. Lo consumen otros servicios (Clientes).
            TransaccionCompletadaEvent completada = new TransaccionCompletadaEvent(
                    evento.solicitudId(),
                    evento.cuentaId(),
                    "RETIRO",
                    evento.monto(),
                    detalle.saldoFinal(),
                    LocalDateTime.now()
            );
            kafkaTemplate.send(TOPIC_TRANSACCIONES_COMPLETADAS, evento.cuentaId().toString(), completada);
            log.info("Publicado TransaccionCompletadaEvent: solicitudId={}, cuentaId={}, saldoFinal={}",
                    evento.solicitudId(), evento.cuentaId(), detalle.saldoFinal());

        } else {
            RetiroResultadoEvent eventoResultado = new RetiroResultadoEvent(
                    evento.solicitudId(),
                    evento.cuentaId(),
                    "RECHAZADO",
                    evento.monto(),
                    null,
                    null,
                    resultado.motivo(),
                    LocalDateTime.now()
            );
            kafkaTemplate.send(TOPIC_RECHAZADOS, evento.cuentaId().toString(), eventoResultado);
            log.warn("Publicado RetiroResultadoEvent RECHAZADO: solicitudId={}, motivo={}",
                    evento.solicitudId(), resultado.motivo());
        }
    }
}