package com.duoc.advanced;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.stereotype.Component;
import com.duoc.business.Venta;

@Component
public class ErrorFileStepExecutionListener implements StepExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(ErrorFileStepExecutionListener.class);
    private final FlatFileItemWriter<Venta> errorItemWriter;

    public ErrorFileStepExecutionListener(FlatFileItemWriter<Venta> errorItemWriter) {
        logger.info("Se ejecuto el ErrorFileStepExecutionListener");
        this.errorItemWriter = errorItemWriter;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        logger.info("Abre el archivo de errores al inicio");
        errorItemWriter.open(new ExecutionContext()); // Abre el archivo de errores al inicio
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        logger.info("Cierra el archivo de errores al final");
        errorItemWriter.close(); // Cierra el archivo de errores al final
        return ExitStatus.COMPLETED;
    }
}