package com.duoc.banco_central_cuentas.service;

import com.duoc.banco_central_cuentas.dto.*;
import com.duoc.banco_central_cuentas.entity.CuentaBancaria;
import com.duoc.banco_central_cuentas.exception.AccesoDenegadoException;
import com.duoc.banco_central_cuentas.exception.CuentaNoEncontradaException;
import com.duoc.banco_central_cuentas.exception.FondosInsuficientesException;
import com.duoc.banco_central_cuentas.exception.OperacionNoPermitidaException;
import com.duoc.banco_central_cuentas.mapper.CuentaBancariaMapper;
import com.duoc.banco_central_cuentas.repository.CuentaBancariaRepository;
import com.duoc.banco_central_cuentas.security.UsuarioAutenticado;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CuentaBancariaService {

    private final CuentaBancariaRepository cuentaBancariaRepository;
    private final CuentaBancariaMapper cuentaBancariaMapper;

    public CuentaBancariaService(CuentaBancariaRepository cuentaBancariaRepository,
                                 CuentaBancariaMapper cuentaBancariaMapper) {
        this.cuentaBancariaRepository = cuentaBancariaRepository;
        this.cuentaBancariaMapper = cuentaBancariaMapper;
    }

    public List<CuentaBancariaDto> listarTodas(){
        return cuentaBancariaRepository.findAll()
                .stream()
                .map(cuentaBancariaMapper::toDto)
                .toList();
    }

    public CuentaBancariaDto obtenerPorId(Long id, UsuarioAutenticado usuario) {
        CuentaBancaria cuenta = buscarCuenta(id);
        validarAcceso(cuenta, usuario);
        return cuentaBancariaMapper.toDto(cuenta);
    }

    public CuentaBancariaSaldoDto obtenerSaldo(Long id, UsuarioAutenticado usuario) {
        CuentaBancaria cuenta = buscarCuenta(id);
        validarAcceso(cuenta, usuario);
        return new CuentaBancariaSaldoDto(cuenta.getSaldoFinal());
    }

    // Consulta de servicio a servicio (token OAuth2): no hay usuario, solo un servicio autorizado.
    public CuentaBancariaSaldoDto obtenerSaldoParaServicio(Long id) {
        CuentaBancaria cuenta = buscarCuenta(id);
        return new CuentaBancariaSaldoDto(cuenta.getSaldoFinal());
    }

    public RetiroCuentaBancariaResponseDto retirar(Long id, RetiroCuentaBancariaRequestDto request) {
        CuentaBancaria cuenta = buscarCuenta(id);

        if (!"ahorro".equalsIgnoreCase(cuenta.getTipo())) {
            throw new OperacionNoPermitidaException("Solo se pueden realizar retiros desde cuentas de ahorro");
        }

        BigDecimal saldoInicial = cuenta.getSaldoFinal();
        BigDecimal montoRetiro = request.monto();

        if (saldoInicial.compareTo(montoRetiro) < 0) {
            throw new FondosInsuficientesException();
        }

        BigDecimal nuevoSaldo = saldoInicial.subtract(montoRetiro);
        cuenta.setSaldoFinal(nuevoSaldo);
        cuentaBancariaRepository.save(cuenta);

        return new RetiroCuentaBancariaResponseDto(saldoInicial, montoRetiro, nuevoSaldo);
    }

    public CuentaBancariaMobileDto obtenerParaMobile(Long id, UsuarioAutenticado usuario) {
        CuentaBancaria cuenta = buscarCuenta(id);
        validarAcceso(cuenta, usuario);
        return new CuentaBancariaMobileDto(cuenta.getNombre(), cuenta.getSaldoFinal(), cuenta.getTipo());
    }

    private CuentaBancaria buscarCuenta(Long id) {
        return cuentaBancariaRepository.findById(id)
                .orElseThrow(() -> new CuentaNoEncontradaException(id));
    }

    // Un empleado ve cualquier cuenta; un cliente solo la suya (la cuenta viene dentro del token).
    private void validarAcceso(CuentaBancaria cuenta, UsuarioAutenticado usuario) {
        boolean esSuCuenta = cuenta.getCuentaIdLegacy().equals(usuario.cuentaIdLegacy());
        if (!usuario.esEmpleado() && !esSuCuenta) {
            throw new AccesoDenegadoException();
        }
    }
}