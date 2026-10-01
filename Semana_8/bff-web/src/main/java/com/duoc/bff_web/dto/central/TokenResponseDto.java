package com.duoc.bff_web.dto.central;

public record TokenResponseDto(
        String access_token,
        String token_type,
        Long expires_in,
        String scope
) {}