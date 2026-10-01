package com.duoc.banco_central_pagos.dto;

public record ResultadoRetiro(boolean aprobado, RetiroCuentaBancariaResponseDto detalle, String motivo) {

    public static ResultadoRetiro conExito(RetiroCuentaBancariaResponseDto detalle) {
        return new ResultadoRetiro(true, detalle, "Retiro procesado correctamente");
    }

    public static ResultadoRetiro conRechazo(String motivo) {
        return new ResultadoRetiro(false, null, motivo);
    }
}