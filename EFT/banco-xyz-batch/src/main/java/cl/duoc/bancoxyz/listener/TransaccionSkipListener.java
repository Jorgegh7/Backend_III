package cl.duoc.bancoxyz.listener;

import cl.duoc.bancoxyz.dtos.TransaccionDto;
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
public class TransaccionSkipListener implements SkipListener<TransaccionDto, Transaccion> {

    @Override
    public void onSkipInProcess(TransaccionDto transaccionDto, Throwable t) {
        log.warn("Registro saltado durante el PROCESAMIENTO: {} | Motivo: {}", transaccionDto, t.getMessage());
    }

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("Registro saltado durante la LECTURA | Motivo: {}", t.getMessage());
    }

    @Override
    public void onSkipInWrite(Transaccion transaccion, Throwable t) {
        log.warn("Registro saltado durante la ESCRITURA: {} | Motivo: {}", transaccion, t.getMessage());
    }
}