package cl.duoc.bancoxyz.listener;

import cl.duoc.bancoxyz.dtos.CuentaBancariaDto;
import cl.duoc.bancoxyz.dtos.TransaccionDto;
import cl.duoc.bancoxyz.entities.CuentaBancaria;
import cl.duoc.bancoxyz.entities.Transaccion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.SkipListener;

/**
 * Registra cada registro saltado (lectura, proceso o escritura), con
 * el motivo puntual de cada excepcion. Complementa el conteo agregado
 * de StepListener con el detalle fila por fila de que se descarto y
 * por que, util para auditar los datos corruptos de cada dataset.
 */
@Slf4j
public class CuentaBancariaSkipListener implements SkipListener<CuentaBancariaDto, CuentaBancaria> {

    @Override
    public void onSkipInProcess(CuentaBancariaDto cuentaBancariaDto, Throwable t) {
        log.warn("Registro saltado durante el PROCESAMIENTO: {} | Motivo: {}", cuentaBancariaDto, t.getMessage());
    }

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("Registro saltado durante la LECTURA | Motivo: {}", t.getMessage());
    }

    @Override
    public void onSkipInWrite(CuentaBancaria cuentaBancaria, Throwable t) {
        log.warn("Registro saltado durante la ESCRITURA: {} | Motivo: {}", cuentaBancaria, t.getMessage());
    }
}
