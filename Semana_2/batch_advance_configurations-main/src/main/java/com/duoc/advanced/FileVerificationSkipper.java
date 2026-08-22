package com.duoc.advanced;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

import com.duoc.business.InvalidDataException;

@Component
public class FileVerificationSkipper implements SkipPolicy {

    private static final Logger logger = LoggerFactory.getLogger(FileVerificationSkipper.class);

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) { // Cambia int a long en skipCount
        // Omite líneas si el error es FlatFileParseException y el número de saltos no supera el límite
        logger.info("Error: FlatFileParseException");
        if ((t instanceof InvalidDataException || t instanceof FlatFileParseException) && skipCount < 10) {
            logger.warn("CustomSkipPolicy - Excepción omitida: {}", t.getMessage()); // Registra un aviso cuando se omite una excepción
            return true;  // Indica que la excepción se debe omitir
        }
        return false; // Indica que la excepción no se debe omitir
    }
}