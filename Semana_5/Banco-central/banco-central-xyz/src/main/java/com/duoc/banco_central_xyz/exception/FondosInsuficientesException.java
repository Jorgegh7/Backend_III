package com.duoc.banco_central_xyz.exception;

public class FondosInsuficientesException extends RuntimeException {

    public FondosInsuficientesException() {
        super("Fondos insuficientes para realizar esta operación");
    }
}