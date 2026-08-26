package cl.duoc.bancoxyz.policies;

import cl.duoc.bancoxyz.exception.FechaInvalidaException;
import cl.duoc.bancoxyz.exception.RegistroInvalidoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class BancoXyzSkipPolicy implements SkipPolicy {

    private static final int LIMITE_SKIP_DATOS = 700;
    private static final int LIMITE_SKIP_FECHAS = 300;

    private final AtomicInteger contadorDatos = new AtomicInteger(0);
    private final AtomicInteger contadorFechas = new AtomicInteger(0);

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {

        if (t instanceof RegistroInvalidoException) {
            int actual = contadorDatos.incrementAndGet();
            if (actual > LIMITE_SKIP_DATOS) {
                throw new SkipLimitExceededException(LIMITE_SKIP_DATOS, t);
            }
            return true;
        }

        if (t instanceof FechaInvalidaException) {
            int actual = contadorFechas.incrementAndGet();
            if (actual > LIMITE_SKIP_FECHAS) {
                throw new SkipLimitExceededException(LIMITE_SKIP_FECHAS, t);
            }
            return true;
        }

        return false;
    }
}