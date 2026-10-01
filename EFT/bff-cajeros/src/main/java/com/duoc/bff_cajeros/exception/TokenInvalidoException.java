package com.duoc.bff_cajeros.exception;

public class TokenInvalidoException extends RuntimeException {

    public TokenInvalidoException() {
        super("Problemas al iniciar sesión. Intenta nuevamente");
    }
}