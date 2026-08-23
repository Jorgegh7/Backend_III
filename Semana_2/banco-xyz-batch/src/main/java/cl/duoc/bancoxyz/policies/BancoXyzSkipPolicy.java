package cl.duoc.bancoxyz.policies;

import cl.duoc.bancoxyz.exception.FechaInvalidaException;
import cl.duoc.bancoxyz.exception.RegistroInvalidoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;

@Slf4j
public class BancoXyzSkipPolicy implements SkipPolicy {

    private static final int LIMITE_SKIP_DATOS = 30;
    private static final int LIMITE_SKIP_FECHAS = 20;

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
        if(t instanceof RegistroInvalidoException){
            if(skipCount >= LIMITE_SKIP_DATOS){
                log.error("Límite de skips por datos inválidos superado: {}", skipCount);
                throw new SkipLimitExceededException(LIMITE_SKIP_DATOS, t);
            }
            return true;
        }

        if(t instanceof FechaInvalidaException){
            if(skipCount >= LIMITE_SKIP_FECHAS){
                log.error("Límite de skips por fechas inválidas superado: {}", skipCount);
                throw new SkipLimitExceededException(LIMITE_SKIP_DATOS, t);
            }
            return true;
        }
        return false;
    }
}
