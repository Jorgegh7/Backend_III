package com.duoc.banco_central_pagos.exception;

public class TransaccionNoEncontradaException extends RuntimeException {
    public TransaccionNoEncontradaException(Long id) {
        super("La transacción solicitada no existe");
    }
}
