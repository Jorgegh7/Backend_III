package com.duoc.banco_central_xyz.exception;

public class MovimientoNoEncontradoException extends RuntimeException {

    public MovimientoNoEncontradoException(Long id) {
        super("El movimiento solicitado no existe");
    }
}