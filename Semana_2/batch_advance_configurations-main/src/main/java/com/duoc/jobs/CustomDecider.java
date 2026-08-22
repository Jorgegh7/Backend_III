package com.duoc.jobs;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.stereotype.Component;

@Component
public class CustomDecider implements JobExecutionDecider {

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        // Lógica de decisión: Aquí puedes definir condiciones personalizadas
        if (stepExecution.getFailureExceptions().isEmpty()) {
            return FlowExecutionStatus.COMPLETED; // Continua si no hay errores
        } else {
            return new FlowExecutionStatus("RETRY"); // O un estado personalizado si hay errores
        }
    }
}
