package com.duoc.bff_web.dto;

import com.duoc.bff_web.dto.central.MovimientoCentralDto;
import com.duoc.bff_web.dto.central.TransaccionCentralDto;

import java.util.List;

public record DashboardDto(
        CuentaBancariaResponseDto cuenta,
        List<TransaccionCentralDto> transacciones,
        List<MovimientoCentralDto> movimientos
) {
}
