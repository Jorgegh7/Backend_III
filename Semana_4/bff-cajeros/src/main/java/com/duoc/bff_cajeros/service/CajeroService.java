package com.duoc.bff_cajeros.service;

import com.duoc.bff_cajeros.client.CuentaBancariaClient;
import com.duoc.bff_cajeros.dto.SaldoResponseDto;
import com.duoc.bff_cajeros.dto.central.SaldoCentralDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CajeroService {

    private final CuentaBancariaClient cuentaBancariaClient;

    public SaldoResponseDto consultarSaldo(Long id, String token) {
        SaldoCentralDto saldo = cuentaBancariaClient.obtenerSaldo(id, token);
        return new SaldoResponseDto(saldo.saldo());
    }

}