package com.duoc.banco_central_xyz.dto;

public record LoginRequestDto(
        String username,
        String password,
        String canal
) {
}
