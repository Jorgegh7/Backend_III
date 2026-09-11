package com.duoc.bff_mobile.service;

import com.duoc.bff_mobile.client.CuentaBancariaClient;
import com.duoc.bff_mobile.dto.SaldoResponseDto;
import com.duoc.bff_mobile.dto.central.CuentaBancariaCentralDto;
import com.duoc.bff_mobile.dto.central.CuentaBancariaMobileCentralDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MobileService {

    private final CuentaBancariaClient cuentaBancariaClient;

    public SaldoResponseDto consultarSaldo(Long id, String token) {
        CuentaBancariaMobileCentralDto cuenta = cuentaBancariaClient.obtenerCuentaResumen(id, token);
        return new SaldoResponseDto(cuenta.nombre(), cuenta.saldo(), cuenta.tipo());
    }

}