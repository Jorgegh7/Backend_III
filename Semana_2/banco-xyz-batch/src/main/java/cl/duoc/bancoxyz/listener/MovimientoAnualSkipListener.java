package cl.duoc.bancoxyz.listener;

import cl.duoc.bancoxyz.dtos.CuentaBancariaDto;
import cl.duoc.bancoxyz.dtos.MovimientoAnualDto;
import cl.duoc.bancoxyz.entities.CuentaBancaria;
import cl.duoc.bancoxyz.entities.MovimientoAnual;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.SkipListener;

@Slf4j
public class MovimientoAnualSkipListener implements SkipListener<MovimientoAnualDto, MovimientoAnual> {

    @Override
    public void onSkipInProcess(MovimientoAnualDto movimientoAnualDto, Throwable t) {
        log.warn("Registro saltado durante el PROCESAMIENTO: {} | Motivo: {}", movimientoAnualDto, t.getMessage());
    }

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("Registro saltado durante la LECTURA | Motivo: {}", t.getMessage());
    }

    @Override
    public void onSkipInWrite(MovimientoAnual movimientoAnual, Throwable t) {
        log.warn("Registro saltado durante la ESCRITURA: {} | Motivo: {}", movimientoAnual, t.getMessage());
    }
}
