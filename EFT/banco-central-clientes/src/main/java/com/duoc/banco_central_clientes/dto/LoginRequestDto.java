package com.duoc.banco_central_clientes.dto;

public record LoginRequestDto(
        String username,
        String password,
        String canal
) {
}
