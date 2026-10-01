package com.duoc.bff_cajeros.kafka;

import com.duoc.bff_cajeros.service.RetiroEstadoStore;
import com.duoc.eventos.RetiroResultadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaResultadoListener {

    private final RetiroEstadoStore estadoStore;

    @KafkaListener(
            topics = {"retiros-aprobados", "retiros-rechazados"},
            groupId = "bff-cajeros-resultados",
            concurrency = "3"
    )
    public void consumirResultado(RetiroResultadoEvent evento) {
        log.info("Consumido RetiroResultadoEvent: solicitudId={}, estado={}",
                evento.solicitudId(), evento.estado());
        estadoStore.guardar(evento);
    }
}