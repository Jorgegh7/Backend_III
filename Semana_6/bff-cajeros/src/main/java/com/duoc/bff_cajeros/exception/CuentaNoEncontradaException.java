package com.duoc.bff_cajeros.exception;

public class CuentaNoEncontradaException extends RuntimeException {

    public CuentaNoEncontradaException(Long id) {
        super("La cuenta solicitada no existe");
    }
}