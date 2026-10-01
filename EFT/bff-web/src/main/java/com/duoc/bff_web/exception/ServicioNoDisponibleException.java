package com.duoc.bff_web.exception;

public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException() {
        super("El servicio no está disponible en este momento. Intenta más tarde");
    }
}
