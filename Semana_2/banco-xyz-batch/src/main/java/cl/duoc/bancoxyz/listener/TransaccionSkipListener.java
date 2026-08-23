package cl.duoc.bancoxyz.listener;

import cl.duoc.bancoxyz.dtos.TransaccionDto;
import cl.duoc.bancoxyz.entities.Transaccion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.SkipListener;

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