package com.duoc.advanced;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Componente que ejecuta el trabajo "ventasJob" automáticamente al iniciar la aplicación.
 * Usa el JobRunner para lanzar el trabajo cuando el contexto de la aplicación está listo.
 */
@Component
public class JobStartupRunner {

    @Autowired
    private JobRunner jobRunner; // Inyección de dependencia para ejecutar el trabajo de ventas

    /**
     * Método que se ejecuta al recibir el evento ApplicationReadyEvent, indicando que la aplicación
     * está lista para funcionar. Inicia el trabajo de ventas llamando al JobRunner.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void runJobAtStartup() {
        jobRunner.runVentasJob(); // Llama a JobRunner para iniciar el trabajo de ventas
    }
}
