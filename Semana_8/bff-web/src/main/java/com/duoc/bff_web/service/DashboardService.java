package com.duoc.bff_web.service;

import com.duoc.bff_web.client.CuentaBancariaClient;
import com.duoc.bff_web.client.MovimientoClient;
import com.duoc.bff_web.client.TransaccionClient;
import com.duoc.bff_web.dto.CuentaBancariaResponseDto;
import com.duoc.bff_web.dto.DashboardDto;
import com.duoc.bff_web.dto.central.CuentaBancariaCentralDto;
import com.duoc.bff_web.dto.central.MovimientoCentralDto;
import com.duoc.bff_web.dto.central.TransaccionCentralDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CuentaBancariaClient cuentaBancariaClient;
    private final TransaccionClient transaccionClient;
    private final MovimientoClient movimientoClient;
    private final ExecutorService backendExecutor;

    public DashboardDto obtenerDashboard(Long id, String token) {

        //Captura el momento en que inicia la llamada para obtener el tiempo total
        long inicio = System.currentTimeMillis();
        log.info("Iniciando armado de dashboard para cuenta id={}", id);

        CompletableFuture<CuentaBancariaCentralDto> cuentaFuture = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Consultando cuenta...", Thread.currentThread().getName());
            CuentaBancariaCentralDto resultado = cuentaBancariaClient.obtenerCuenta(id, token);
            log.info("[{}] Cuenta obtenida", Thread.currentThread().getName());
            return resultado;
        }, backendExecutor);

        CompletableFuture<List<TransaccionCentralDto>> transaccionesFuture = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Consultando transacciones...", Thread.currentThread().getName());
            List<TransaccionCentralDto> resultado = transaccionClient.listarTransacciones(token);
            log.info("[{}] Transacciones obtenidas", Thread.currentThread().getName());
            return resultado;
        }, backendExecutor);

        CompletableFuture<List<MovimientoCentralDto>> movimientosFuture = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Consultando movimientos...", Thread.currentThread().getName());
            List<MovimientoCentralDto> resultado = movimientoClient.listarMovimientos(token);
            log.info("[{}] Movimientos obtenidos", Thread.currentThread().getName());
            return resultado;
        }, backendExecutor);

        try {
            CompletableFuture.allOf(cuentaFuture, transaccionesFuture, movimientosFuture).join();
        } catch (CompletionException ex) {
            if (ex.getCause() instanceof RuntimeException runtimeCause) {
                throw runtimeCause;
            }
            throw ex;
        }

        long tiempoTotal = System.currentTimeMillis() - inicio;
        log.info("Las 3 llamadas finalizaron en {} ms, armando respuesta combinada", tiempoTotal);

        CuentaBancariaCentralDto cuenta = cuentaFuture.join();

        List<TransaccionCentralDto> transacciones = transaccionesFuture.join()
                .stream()
                .sorted(Comparator.comparing(TransaccionCentralDto::fecha).reversed())
                .limit(5)
                .toList();

        List<MovimientoCentralDto> movimientos = movimientosFuture.join()
                .stream()
                .sorted(Comparator.comparing(MovimientoCentralDto::fecha).reversed())
                .limit(5)
                .toList();

        CuentaBancariaResponseDto cuentaDto = new CuentaBancariaResponseDto(
                cuenta.cuentaIdLegacy(),
                cuenta.nombre(),
                cuenta.saldo(),
                cuenta.edad(),
                cuenta.tipo(),
                cuenta.interes(),
                cuenta.saldoFinal()
        );

        return new DashboardDto(cuentaDto, transacciones, movimientos);
    }
}