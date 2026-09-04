package com.duoc.banco_central_xyz.exception;

public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException() {
        super("No tienes permisos para acceder a este recurso");
    }
}