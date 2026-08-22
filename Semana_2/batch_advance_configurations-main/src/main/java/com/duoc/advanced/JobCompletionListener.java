package com.duoc.advanced;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionListener implements JobExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(JobCompletionListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        logger.info("Inicio del trabajo batch con ID: {}", jobExecution.getJobId());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus().isUnsuccessful()) {
            logger.error("Job terminó con errores. Revisar reintentos y excepciones.");
        } else {
            logger.info("Job completado exitosamente con ID: {}", jobExecution.getJobId());
        }
        logger.info("Resumen del Job: {}", jobExecution.toString());
    }
}
