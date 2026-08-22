package com.duoc.advanced;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

import com.duoc.business.InformeVenta;
import com.duoc.business.Venta;

import java.util.List;

@Component
public class VentaSkipListener implements SkipListener<Venta, InformeVenta> {

    private static final Logger logger = LoggerFactory.getLogger(VentaSkipListener.class);
    private final FlatFileItemWriter<Venta> errorItemWriter;

    public VentaSkipListener(FlatFileItemWriter<Venta> errorItemWriter) {
        logger.info("Creando VentaSkipListener.");
        this.errorItemWriter = errorItemWriter;
    }

    @Override
    public void onSkipInProcess(Venta item, Throwable t) {
        logger.info("Tipo de excepción en SkipListener: {}", t.getClass().getName());
        logger.warn("SkipListener activado - Error al procesar registro: {}", item);
        try {
            errorItemWriter.write(new Chunk<>(List.of(item)));
            logger.info("Registro escrito en errores.csv: {}", item);
        } catch (Exception e) {
            logger.error("Error al escribir registro omitido en archivo de errores: ", e);
        }
    }

    @Override
    public void onSkipInRead(Throwable t) {
        logger.info("Entro a onSkipInRead: {}", t.getClass().getName());
        if (t instanceof FlatFileParseException) {
            FlatFileParseException ffpe = (FlatFileParseException) t;
            logger.warn("Línea omitida debido a un error en la lectura: {}", ffpe.getInput());
        }
    }

    @Override
    public void onSkipInWrite(InformeVenta item, Throwable t) {
        logger.error("Error al escribir registro: ", t);
    }
    
}
