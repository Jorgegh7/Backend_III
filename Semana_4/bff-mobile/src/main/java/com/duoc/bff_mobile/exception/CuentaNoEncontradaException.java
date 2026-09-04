package com.duoc.bff_mobile.exception;

public class CuentaNoEncontradaException extends RuntimeException {

    public CuentaNoEncontradaException(Long id) {
        super("La cuenta solicitada no encontrada. Intenta nuevamente");
    }
}