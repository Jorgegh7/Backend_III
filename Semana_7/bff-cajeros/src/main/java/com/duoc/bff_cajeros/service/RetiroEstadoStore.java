package com.duoc.bff_cajeros.service;

import com.duoc.eventos.RetiroResultadoEvent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RetiroEstadoStore {

    private final Map<String, RetiroResultadoEvent> resultados = new ConcurrentHashMap<>();

    public void guardar(RetiroResultadoEvent evento) {
        resultados.put(evento.solicitudId(), evento);
    }

    public RetiroResultadoEvent obtener(String solicitudId) {
        return resultados.get(solicitudId);
    }
}