package com.duoc.bff_web.service;

import com.duoc.bff_web.client.CuentaBancariaClient;

import com.duoc.bff_web.dto.CuentaBancariaResponseDto;
import com.duoc.bff_web.dto.central.CuentaBancariaCentralDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebService {

    private final CuentaBancariaClient cuentaBancariaClient;

    public CuentaBancariaResponseDto consultarCuenta(Long id, String token) {
        CuentaBancariaCentralDto cuenta = cuentaBancariaClient.obtenerCuenta(id, token);

        return new CuentaBancariaResponseDto(
                cuenta.cuentaIdLegacy(),
                cuenta.nombre(),
                cuenta.saldo(),
                cuenta.edad(),
                cuenta.tipo(),
                cuenta.interes(),
                cuenta.saldoFinal()
        );
    }
}