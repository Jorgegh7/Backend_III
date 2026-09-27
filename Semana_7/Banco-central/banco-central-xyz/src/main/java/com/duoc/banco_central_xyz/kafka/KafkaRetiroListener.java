package com.duoc.banco_central_xyz.kafka;

import com.duoc.banco_central_xyz.dto.RetiroCuentaBancariaRequestDto;
import com.duoc.banco_central_xyz.dto.RetiroCuentaBancariaResponseDto;
import com.duoc.banco_central_xyz.exception.CuentaNoEncontradaException;
import com.duoc.banco_central_xyz.exception.FondosInsuficientesException;
import com.duoc.banco_central_xyz.exception.OperacionNoPermitidaException;
import com.duoc.banco_central_xyz.service.CuentaBancariaService;
import com.duoc.eventos.RetiroResultadoEvent;
import com.duoc.eventos.RetiroSolicitadoEvent;
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

    private final CuentaBancariaService cuentaBancariaService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_APROBADOS = "retiros-aprobados";
    private static final String TOPIC_RECHAZADOS = "retiros-rechazados";

    @KafkaListener(
            topics = "retiros-solicitados",
            groupId = "banco-central-retiros",
            concurrency = "3"
    )
    public void procesarRetiro(RetiroSolicitadoEvent evento) {
        log.info("Consumido RetiroSolicitadoEvent: solicitudId={}, cuentaId={}, monto={}",
                evento.solicitudId(), evento.cuentaId(), evento.monto());

        try {
            RetiroCuentaBancariaRequestDto request = new RetiroCuentaBancariaRequestDto(evento.monto());
            RetiroCuentaBancariaResponseDto resultado = cuentaBancariaService.retirar(evento.cuentaId(), request);

            RetiroResultadoEvent eventoResultado = new RetiroResultadoEvent(
                    evento.solicitudId(),
                    evento.cuentaId(),
                    "APROBADO",
                    evento.monto(),
                    resultado.saldoInicial(),
                    resultado.saldoFinal(),
                    "Retiro procesado correctamente",
                    LocalDateTime.now()
            );

            kafkaTemplate.send(TOPIC_APROBADOS, evento.cuentaId().toString(), eventoResultado);
            log.info("Publicado RetiroResultadoEvent APROBADO: solicitudId={}", evento.solicitudId());

        } catch (CuentaNoEncontradaException | FondosInsuficientesException | OperacionNoPermitidaException ex) {
            RetiroResultadoEvent eventoResultado = new RetiroResultadoEvent(
                    evento.solicitudId(),
                    evento.cuentaId(),
                    "RECHAZADO",
                    evento.monto(),
                    null,
                    null,
                    ex.getMessage(),
                    LocalDateTime.now()
            );

            kafkaTemplate.send(TOPIC_RECHAZADOS, evento.cuentaId().toString(), eventoResultado);
            log.warn("Publicado RetiroResultadoEvent RECHAZADO: solicitudId={}, motivo={}",
                    evento.solicitudId(), ex.getMessage());
        }
    }
}