package com.duoc.bff_cajeros.service;

import com.duoc.bff_cajeros.client.CuentaBancariaClient;
import com.duoc.bff_cajeros.dto.RetiroRequestDto;
import com.duoc.bff_cajeros.dto.RetiroSolicitudResponseDto;
import com.duoc.bff_cajeros.dto.SaldoResponseDto;
import com.duoc.bff_cajeros.dto.central.SaldoCentralDto;
import com.duoc.eventos.RetiroSolicitadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CajeroService {

    private final CuentaBancariaClient cuentaBancariaClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_RETIROS_SOLICITADOS = "retiros-solicitados";

    public SaldoResponseDto consultarSaldo(Long id, String token) {
        SaldoCentralDto saldo = cuentaBancariaClient.obtenerSaldo(id, token);
        return new SaldoResponseDto(saldo.saldo());
    }

    public RetiroSolicitudResponseDto solicitarRetiro(Long cuentaId, RetiroRequestDto request, String token) {
        // Valida que la cuenta pertenezca al usuario del token: si no, el Banco Central
        // responde 403 y el retiro no se publica en Kafka.
        cuentaBancariaClient.obtenerSaldo(cuentaId, token);

        String solicitudId = UUID.randomUUID().toString();

        RetiroSolicitadoEvent evento = new RetiroSolicitadoEvent(
                solicitudId,
                cuentaId,
                request.monto(),
                LocalDateTime.now()
        );

        kafkaTemplate.send(TOPIC_RETIROS_SOLICITADOS, cuentaId.toString(), evento);
        log.info("Publicado RetiroSolicitadoEvent: solicitudId={}, cuentaId={}, monto={}",
                solicitudId, cuentaId, request.monto());

        return new RetiroSolicitudResponseDto(
                solicitudId,
                "PENDIENTE",
                "La solicitud de retiro fue enviada para procesamiento asincrono"
        );
    }
}