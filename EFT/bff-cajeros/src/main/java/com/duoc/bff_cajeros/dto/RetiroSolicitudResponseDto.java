package com.duoc.bff_cajeros.dto;

public record RetiroSolicitudResponseDto(
        String solicitudId,
        String estado,
        String mensaje
) {}