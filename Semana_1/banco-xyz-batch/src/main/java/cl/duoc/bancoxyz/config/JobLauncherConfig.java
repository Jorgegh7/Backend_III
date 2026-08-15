package cl.duoc.bancoxyz.config;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        };
    }
}