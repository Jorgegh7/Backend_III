package com.duoc.items;

import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.duoc.business.Venta;

@Component
public class ErrorItemWriter extends FlatFileItemWriter<Venta> {

    public ErrorItemWriter() {
        setResource(new FileSystemResource("errores.csv")); // Define el archivo de salida de errores

        // Configura el agregador de líneas delimitadas y el extractor de campos
        DelimitedLineAggregator<Venta> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(","); // Define el delimitador como coma

        BeanWrapperFieldExtractor<Venta> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"id", "producto", "cantidad", "precio"}); // Define los nombres de los campos

        lineAggregator.setFieldExtractor(fieldExtractor); // Configura el agregador de líneas con el extractor
        setLineAggregator(lineAggregator); // Asigna el agregador de líneas al escritor
    }
}