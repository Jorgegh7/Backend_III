package cl.duoc.bancoxyz.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;

/**
 * Registra inicio y fin del Job completo: estado final y un resumen
 * de leidos/escritos/saltados del Step, tomado de StepExecution al
 * finalizar. Sirve como resumen de alto nivel de toda la ejecucion.
 */
@Slf4j
public class MovimientoAnualJobListener implements JobExecutionListener {

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
