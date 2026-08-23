package cl.duoc.bancoxyz.listener;

import cl.duoc.bancoxyz.dtos.CuentaBancariaDto;
import cl.duoc.bancoxyz.dtos.TransaccionDto;
import cl.duoc.bancoxyz.entities.CuentaBancaria;
import cl.duoc.bancoxyz.entities.Transaccion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.SkipListener;

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
