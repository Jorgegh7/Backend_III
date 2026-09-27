package com.duoc.bff_mobile.exception;

public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException() {
        super("No tienes permisos para acceder a este recurso");
    }
}
