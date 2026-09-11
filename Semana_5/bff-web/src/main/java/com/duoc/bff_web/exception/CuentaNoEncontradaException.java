package com.duoc.bff_web.exception;

public class CuentaNoEncontradaException extends RuntimeException {
    public CuentaNoEncontradaException(Long id) {
        super("La cuenta solicitada no encontrada. Intenta nuevamente");
    }
}
