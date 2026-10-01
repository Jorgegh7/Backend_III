package com.duoc.banco_central_cuentas.dto;

import java.math.BigDecimal;

public record RetiroCuentaBancariaRequestDto(
        BigDecimal monto
) {}