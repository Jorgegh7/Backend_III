package com.duoc.advanced;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import com.duoc.business.InformeVenta;
import com.duoc.business.Venta;
import com.duoc.items.ErrorItemWriter;
import com.duoc.items.VentasItemProcessor;
import com.duoc.items.VentasItemReader;
import com.duoc.jobs.CustomDecider;


@Configuration // Indica que esta clase contiene la configuración de beans de Spring
@EnableBatchProcessing // Habilita el procesamiento batch en el contexto de Spring
@Import(DataSourceConfiguration.class) // Importa la configuración de la base de datos
@ComponentScans({
    @ComponentScan(basePackages = "com.duoc.items"),
    @ComponentScan(basePackages = "com.duoc.jobs")
})
public class VentasJobConfig {

    // Configuración del Job con ID incremental y Decisor para manejo de finalización
    @Bean
    public Job ventasJob(JobRepository jobRepository, Step step, CustomDecider decider, JobCompletionListener listener) {
        return new JobBuilder("ventasJob", jobRepository) // Crea un Job con el nombre "ventasJob" usando el JobRepository
                .incrementer(new RunIdIncrementer()) // Agrega un incrementador de ID de ejecución
                .listener(listener) // Registra un listener para el Job
                .start(step) // Establece el Step inicial para el Job
                .next(decider) // Usa un Decider para determinar el flujo del Job
                .on("COMPLETED").end() // Si el Decider devuelve "COMPLETED", termina el Job
                .from(decider).on("RETRY").to(step) // Si el Decider devuelve "RETRY", vuelve a ejecutar el Step
                .end() // Finaliza la configuración del flujo
                .build(); // Construye y devuelve el Job configurado
    }

    // Step configurado para procesamiento paralelo, reintentos y política de repetición
    @Bean
    public Step consolidacionDiariaStep(JobRepository jobRepository, 
                                        JdbcTransactionManager transactionManager,
                                        VentasItemReader itemReader, 
                                        VentasItemProcessor itemProcessor,
                                        FlatFileItemWriter<InformeVenta> itemWriter,
                                        ErrorItemWriter errorItemWriter,
                                        FileVerificationSkipper fileVerificationSkipper,
                                        SkipListener<Venta, InformeVenta> skipListener,
                                        StepExecutionListener errorFileStepExecutionListener,
                                        ThreadPoolTaskExecutor taskExecutor
                                        ) {
        return new StepBuilder("consolidacionDiariaStep", jobRepository) // Crea un Step llamado "consolidacionDiariaStep"
                .<Venta, InformeVenta>chunk(new SimpleCompletionPolicy(2), transactionManager) // Define el tamaño del chunk y la política de transacción
                .reader(itemReader) // Configura el lector de ítems
                .processor(itemProcessor) // Configura el procesador de ítems
                .writer(itemWriter) // Configura el escritor de ítems
                .faultTolerant() // Habilita la tolerancia a fallos en el Step
                .skipPolicy(fileVerificationSkipper) // Asigna una política de salto personalizada
                .listener(skipListener)  // Añade un SkipListener para manejar errores
                .listener(errorFileStepExecutionListener) // Añade un listener para ejecutar acciones específicas en el Step
                .taskExecutor(taskExecutor) // Configura la ejecución en paralelo con un TaskExecutor
                .build(); // Construye y devuelve el Step configurado
    }

    // Configura el TaskExecutor para procesamiento paralelo
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(); // Crea un TaskExecutor para la ejecución en paralelo
        executor.setCorePoolSize(5); // Define el tamaño inicial del pool de hilos
        executor.setMaxPoolSize(10); // Define el tamaño máximo del pool de hilos
        executor.setQueueCapacity(25); // Define la capacidad de la cola
        executor.setThreadNamePrefix("Batch-Thread-"); // Prefijo de nombre de hilo para identificar los hilos del batch
        executor.initialize(); // Inicializa el TaskExecutor
        return executor;
    }
}
