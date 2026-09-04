package com.duoc.banco_central_xyz.service;

import com.duoc.banco_central_xyz.dto.CuentaBancariaDto;
import com.duoc.banco_central_xyz.dto.CuentaBancariaSaldoDto;
import com.duoc.banco_central_xyz.dto.RetiroCuentaBancariaRequestDto;
import com.duoc.banco_central_xyz.dto.RetiroCuentaBancariaResponseDto;
import com.duoc.banco_central_xyz.entity.CuentaBancaria;
import com.duoc.banco_central_xyz.entity.Usuario;
import com.duoc.banco_central_xyz.exception.AccesoDenegadoException;
import com.duoc.banco_central_xyz.exception.CuentaNoEncontradaException;
import com.duoc.banco_central_xyz.exception.FondosInsuficientesException;
import com.duoc.banco_central_xyz.exception.OperacionNoPermitidaException;
import com.duoc.banco_central_xyz.mapper.CuentaBancariaMapper;
import com.duoc.banco_central_xyz.repository.CuentaBancariaRepository;
import com.duoc.banco_central_xyz.repository.UsuarioRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CuentaBancariaService {

    private final CuentaBancariaRepository cuentaBancariaRepository;
    private final CuentaBancariaMapper cuentaBancariaMapper;
    private final UsuarioRepository usuarioRepository;

    public CuentaBancariaService(CuentaBancariaRepository cuentaBancariaRepository,
                                 CuentaBancariaMapper cuentaBancariaMapper, UsuarioRepository usuarioRepository) {
        this.cuentaBancariaRepository = cuentaBancariaRepository;
        this.cuentaBancariaMapper = cuentaBancariaMapper;
        this.usuarioRepository = usuarioRepository;
    }

    public List<CuentaBancariaDto> listarTodas(){
        return cuentaBancariaRepository.findAll()
                .stream()
                .map(cuentaBancariaMapper::toDto)
                .toList();
    }

    public CuentaBancariaDto obtenerPorId(Long id, String username) {
        CuentaBancaria cuenta = cuentaBancariaRepository.findById(id)
                .orElseThrow(() -> new CuentaNoEncontradaException(id));

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        boolean esEmpleado = "EMPLEADO".equals(usuario.getRol().name());
        boolean esSuCuenta = cuenta.getCuentaIdLegacy().equals(usuario.getCuentaIdLegacy());

        if (!esEmpleado && !esSuCuenta) {
            throw new AccesoDenegadoException();
        }

        return cuentaBancariaMapper.toDto(cuenta);
    }

    public CuentaBancariaSaldoDto obtenerSaldo(Long id, String username) {
        CuentaBancaria cuenta = cuentaBancariaRepository.findById(id)
                .orElseThrow(() -> new CuentaNoEncontradaException(id));

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        boolean esEmpleado = "EMPLEADO".equals(usuario.getRol().name());
        boolean esSuCuenta = cuenta.getCuentaIdLegacy().equals(usuario.getCuentaIdLegacy());

        if (!esEmpleado && !esSuCuenta) {
            throw new AccesoDenegadoException();
        }

        return new CuentaBancariaSaldoDto(cuenta.getSaldoFinal());
    }

    public RetiroCuentaBancariaResponseDto retirar(Long id, RetiroCuentaBancariaRequestDto request) {
        CuentaBancaria cuenta = cuentaBancariaRepository.findById(id)
                .orElseThrow(() -> new CuentaNoEncontradaException(id));

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


}
