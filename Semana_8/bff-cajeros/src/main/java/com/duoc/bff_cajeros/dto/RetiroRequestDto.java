package com.duoc.bff_cajeros.dto;

import java.math.BigDecimal;

public record RetiroRequestDto(
        BigDecimal monto
) {}