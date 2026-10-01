package cl.duoc.bancoxyz.config;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dispara los 3 Jobs en secuencia al arrancar la aplicacion, usando
 * JobOperator (no JobLauncher, deprecado en Spring Batch 6) dentro de
 * un CommandLineRunner. La secuencialidad es intencional: cada proceso
 * legacy es independiente entre si; el paralelismo real ocurre dentro
 * de cada Job, a nivel de chunks (ver TaskExecutorConfig).
 *
 * spring.batch.job.enabled=false evita que Spring Boot dispare los Jobs
 * por su cuenta, dejando el control del orden y momento a este runner.
 */
@Configuration
public class JobLauncherConfig {

    @Bean
    public CommandLineRunner lanzarJobs(JobOperator jobOperator,
                                        Job transaccionJob,
                                        Job cuentaBancariaJob,
                                        Job movimientoAnualJob) {
        return args -> {
            var parametrosTransaccion = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobOperator.start(transaccionJob, parametrosTransaccion);

            var parametrosCuenta = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobOperator.start(cuentaBancariaJob, parametrosCuenta);

            var parametrosMovimiento = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobOperator.start(movimientoAnualJob, parametrosMovimiento);

            System.exit(0);
        };
    }
}