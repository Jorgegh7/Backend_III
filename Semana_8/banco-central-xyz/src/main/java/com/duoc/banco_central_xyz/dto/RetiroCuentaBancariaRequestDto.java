package com.duoc.banco_central_xyz.dto;

import java.math.BigDecimal;

public record RetiroCuentaBancariaRequestDto(
        BigDecimal monto
) {}