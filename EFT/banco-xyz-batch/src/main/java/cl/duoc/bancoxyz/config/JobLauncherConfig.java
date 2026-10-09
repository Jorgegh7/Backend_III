package cl.duoc.bancoxyz.config;

import cl.duoc.bancoxyz.exception.BatchJobLaunchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dispara los 3 Jobs en secuencia al arrancar la aplicacion.
 *
 * Reejecucion automatica ante fallos criticos: si un Job termina en un
 * estado distinto de COMPLETED (p. ej. FAILED por error al abrir el
 * reader), se relanza. Como cada Job define RunIdIncrementer, cada
 * intento crea una JobInstance NUEVA (run.id 1, 2, 3...) y reprocesa
 * desde el inicio. Cada intento queda registrado en BATCH_JOB_INSTANCE
 * y BATCH_JOB_EXECUTION.
 * Si se agotan los intentos se lanza BatchJobLaunchException y el
 * proceso termina con codigo 1.
 *
 * Los errores por registro (datos invalidos) NO activan esto: los
 * manejan las politicas Skip/Retry de cada Step.
 */
@Configuration
public class JobLauncherConfig {

    private static final Logger log = LoggerFactory.getLogger(JobLauncherConfig.class);

    @Value("${batch.reejecucion.max-intentos:3}")
    private int maxIntentos;

    @Value("${batch.reejecucion.espera-ms:5000}")
    private long esperaMs;

    @Bean
    public CommandLineRunner lanzarJobs(JobOperator jobOperator,
                                        Job transaccionJob,
                                        Job cuentaBancariaJob,
                                        Job movimientoAnualJob) {
        return args -> {
            try {
                ejecutarConReintento(jobOperator, transaccionJob);
                ejecutarConReintento(jobOperator, cuentaBancariaJob);
                ejecutarConReintento(jobOperator, movimientoAnualJob);
            } catch (BatchJobLaunchException e) {
                log.error("Proceso batch abortado: {}", e.getMessage());
                System.exit(1);
            }
            System.exit(0);
        };
    }

    private void ejecutarConReintento(JobOperator jobOperator, Job job) {
        // Con RunIdIncrementer estos parametros se ignoran; cada intento es una instancia nueva
        JobParameters parametros = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        for (int intento = 1; intento <= maxIntentos; intento++) {
            log.info("[{}] Intento {}/{}", job.getName(), intento, maxIntentos);
            try {
                JobExecution ejecucion = jobOperator.start(job, parametros);
                BatchStatus estado = ejecucion.getStatus();

                if (estado == BatchStatus.COMPLETED) {
                    log.info("[{}] COMPLETED en el intento {}", job.getName(), intento);
                    return;
                }
                log.warn("[{}] Termino con estado {} (intento {}/{})",
                        job.getName(), estado, intento, maxIntentos);
            } catch (Exception e) {
                log.error("[{}] Fallo critico (intento {}/{}): {}",
                        job.getName(), intento, maxIntentos, e.getMessage());
            }

            if (intento < maxIntentos) {
                esperar();
            }
        }
        throw new BatchJobLaunchException(
                "Job " + job.getName() + " fallo tras " + maxIntentos + " intentos");
    }

    private void esperar() {
        try {
            log.info("Esperando {} ms antes de reejecutar...", esperaMs);
            Thread.sleep(esperaMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}