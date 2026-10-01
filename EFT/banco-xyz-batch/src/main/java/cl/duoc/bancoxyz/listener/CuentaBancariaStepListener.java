package cl.duoc.bancoxyz.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Registra inicio y fin del Step, y persiste ese resumen en la tabla
 * execution_log via JdbcTemplate. Es la evidencia de ejecucion que
 * reemplaza a BATCH_STEP_EXECUTION, ya que Spring Batch 6 no persiste
 * sus propias tablas de metadata por defecto (ver dependencia
 * spring-boot-starter-batch-jdbc, deliberadamente no incluida).
 */
@Slf4j
public class CuentaBancariaStepListener implements StepExecutionListener {

    private final JdbcTemplate jdbcTemplate;

    public CuentaBancariaStepListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("--- Iniciando Step: {} ---", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("--- Step {} finalizado. Leídos: {}, escritos: {}, saltados: {} ---",
                stepExecution.getStepName(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount());

        jdbcTemplate.update(
                "INSERT INTO execution_log (job_nombre, step_nombre, estado, leidos, escritos, saltados) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                stepExecution.getJobExecution().getJobInstance().getJobName(),
                stepExecution.getStepName(),
                stepExecution.getStatus().toString(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount()
        );
        return stepExecution.getExitStatus();
    }
}
