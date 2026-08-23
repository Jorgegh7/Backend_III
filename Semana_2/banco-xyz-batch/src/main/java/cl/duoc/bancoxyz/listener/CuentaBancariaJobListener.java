package cl.duoc.bancoxyz.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;

@Slf4j
public class CuentaBancariaJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("=== Iniciando Job: {} ===", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("=== Job {} finalizado con estado: {} ===",
                jobExecution.getJobInstance().getJobName(), jobExecution.getStatus());

        jobExecution.getStepExecutions().forEach(step ->
                log.info("Step '{}' -> leídos: {}, escritos: {}, saltados (lectura+proceso+escritura): {}",
                        step.getStepName(),
                        step.getReadCount(),
                        step.getWriteCount(),
                        step.getSkipCount())
        );
    }
}
