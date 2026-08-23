package cl.duoc.bancoxyz.policies;

import org.springframework.batch.infrastructure.repeat.RepeatContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.batch.infrastructure.repeat.policy.CompletionPolicySupport;


/**
 * Decide cuando se cierra un chunk: por cantidad de items (MAX_ITEMS)
 * o por tiempo transcurrido (MAX_TIEMPO_MS), lo que ocurra primero.
 * Reutilizable para los 3 Jobs (Transaccion, CuentaBancaria, MovimientoAnual).
 */
public class BancoXyzCompletionPolicy extends CompletionPolicySupport {

    private static final int MAX_ITEMS = 5;
    private static final long MAX_TIEMPO_MS = 2000;

    private long tiempoInicioChunk;

    /**
    * Inicia el Chunk.
    * Sucede antes de leer el primer item.
    * Permite obtener la variable tiempoInicioChunk
    */
    @Override
    public RepeatContext start(RepeatContext parent) {
        tiempoInicioChunk = System.currentTimeMillis();
        return super.start(parent);
    }

    @Override
    public boolean isComplete(RepeatContext context, RepeatStatus result) {
        return isComplete(context);
    }

    /**
     * Se pregunta ANTES de leer/procesar el siguiente item: "¿sigo llenando este chunk, o ya está listo?"
     * true  = cierra el chunk ahora (se manda al Writer)
     * false = sigue intentando meter otro item
     *
     * Se cierra si: ya se procesaron 5 items (MAX_ITEMS) O ya pasaron 2 segundos (MAX_TIEMPO_MS)
     * desde que arrancó el chunk -- lo que pase primero.
     */
    @Override
    public boolean isComplete(RepeatContext context) {
        boolean porCantidad = context.getStartedCount() >= MAX_ITEMS;
        boolean porTiempo = (System.currentTimeMillis() - tiempoInicioChunk) >= MAX_TIEMPO_MS;
        return porCantidad || porTiempo;
    }
}