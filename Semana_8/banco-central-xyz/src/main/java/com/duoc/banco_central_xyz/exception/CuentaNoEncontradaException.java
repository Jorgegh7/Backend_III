package com.duoc.banco_central_xyz.exception;

public class CuentaNoEncontradaException extends RuntimeException {

    public CuentaNoEncontradaException(Long id) {
        super("La cuenta solicitada no existe"); // sin exponer el id específico
    }
}
