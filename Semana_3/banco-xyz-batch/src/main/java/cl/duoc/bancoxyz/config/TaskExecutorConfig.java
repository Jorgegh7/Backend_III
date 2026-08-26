package cl.duoc.bancoxyz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Pool de hilos fijo en exactamente 3 hilos paralelos, con politica de
 * escalamiento explicita: core = max = 3 (no escala mas alla de 3),
 * cola de espera acotada, y CallerRunsPolicy como salvaguarda si el
 * pool estuviera saturado (en vez de descartar tareas silenciosamente).
 */
@Configuration
public class TaskExecutorConfig {

    private static final int NUM_HILOS = 6;
    private static final int QUEUE_CAPACITY = 100;

    @Bean
    public TaskExecutor bancoXyzTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // core = max = 3: fija el pool exactamente en 3 hilos, sin escalar mas alla
        executor.setCorePoolSize(NUM_HILOS);
        executor.setMaxPoolSize(NUM_HILOS);

        // cuantas tareas pueden esperar en cola si los 3 hilos estan ocupados
        executor.setQueueCapacity(QUEUE_CAPACITY);

        // si la cola tambien se llena, en vez de descartar la tarea,
        // el hilo que la lanzo la ejecuta el mismo (evita perder chunks)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.setThreadNamePrefix("bancoxyz-step-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();

        return executor;
    }
}