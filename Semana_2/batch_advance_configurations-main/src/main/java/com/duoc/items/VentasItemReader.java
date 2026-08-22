package com.duoc.items;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.batch.item.ExecutionContext;
import com.duoc.business.Venta;

@Component
public class VentasItemReader implements ItemReader<Venta>, ItemStream {

    private final FlatFileItemReader<Venta> delegate; // El lector delegado

    public VentasItemReader() {
        // Configura el FlatFileItemReader interno
        this.delegate = new FlatFileItemReaderBuilder<Venta>()
                .name("ventasItemReader") // Define el nombre del lector
                .resource(new ClassPathResource("consolidacion_diaria_ventas.csv")) // Ubicación del archivo CSV de entrada
                .linesToSkip(1) // Omite la primera línea (encabezados)
                .delimited() // Define el formato de los datos como delimitado
                .names("id", "producto", "cantidad", "precio") // Define los nombres de los campos
                .targetType(Venta.class) // Clase objetivo para el mapeo de los datos
                .build();
        this.delegate.setStrict(false); // Permite continuar en caso de error en el archivo
    }

    @Override
    public Venta read() throws Exception {
        return delegate.read(); // Delegado para la lectura de cada ítem
    }

    // Implementa la apertura de ItemStream para abrir el recurso de archivo antes de leer
    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        delegate.open(executionContext); // Abre el lector delegado
    }

    // Implementa el cierre de ItemStream para cerrar el recurso de archivo después de leer
    @Override
    public void close() throws ItemStreamException {
        delegate.close(); // Cierra el lector delegado
    }

    // Implementa la actualización del ExecutionContext si es necesario (opcional en este caso)
    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        delegate.update(executionContext);
    }
}
