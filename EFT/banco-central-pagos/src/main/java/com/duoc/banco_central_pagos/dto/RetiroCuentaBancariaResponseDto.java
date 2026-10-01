package com.duoc.banco_central_pagos.dto;

import java.math.BigDecimal;

public record RetiroCuentaBancariaResponseDto(
        BigDecimal saldoInicial,
        BigDecimal montoRetirado,
        BigDecimal saldoFinal
) {}