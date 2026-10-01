package cl.duoc.bancoxyz.policies;

import cl.duoc.bancoxyz.exception.FechaInvalidaException;
import cl.duoc.bancoxyz.exception.RegistroInvalidoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.SimpleRetryPolicy;

@Slf4j
public class BancoXyzRetryPolicy extends SimpleRetryPolicy {

    private static final int MAX_ATTEMPTS = 3;

    //Necesita un valor el constructor
    public BancoXyzRetryPolicy() {
        super(MAX_ATTEMPTS);
    }

    @Override
    public boolean canRetry(RetryContext context){
        Throwable ultimoError = context.getLastThrowable();
        if(ultimoError instanceof FechaInvalidaException || ultimoError instanceof RegistroInvalidoException){
            return false;
        }

        //Llamada a Super para comparar de forma interna RetryCount < MAX_ATTEMPTS
        boolean puedeReintentar = canRetry(context);
        if(ultimoError != null){
            log.warn("Reintento {}/{} tras excepción inesperada: {}",
                    context.getRetryCount(), MAX_ATTEMPTS, ultimoError.getMessage());
        }
        return puedeReintentar;
    }
}
