package com.duoc.banco_central_pagos.kafka;

import com.duoc.eventos.AlertaSeguridadEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consume las alertas de seguridad que publica banco-central-clientes.
 * Pagos es el servicio que mueve dinero, por eso es el destinatario natural:
 * por ahora registra la alerta; a futuro podria bloquear retiros del usuario.
 */
@Slf4j
@Component
public class AlertaSeguridadListener {

    @KafkaListener(
            topics = "alertas-seguridad",
            groupId = "banco-central-pagos-alertas",
            concurrency = "3"
    )
    public void procesarAlerta(AlertaSeguridadEvent alerta) {
        log.warn("ALERTA DE SEGURIDAD recibida: usuario='{}', canal={}, intentosFallidos={}, motivo='{}', fecha={}",
                alerta.username(),
                alerta.canal(),
                alerta.intentosFallidos(),
                alerta.motivo(),
                alerta.fechaAlerta());
    }
}