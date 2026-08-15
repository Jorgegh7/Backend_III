package cl.duoc.bancoxyz.config;

import cl.duoc.bancoxyz.dtos.TransaccionDto;
import cl.duoc.bancoxyz.entities.Transaccion;
import cl.duoc.bancoxyz.exception.FechaInvalidaException;
import cl.duoc.bancoxyz.exception.RegistroInvalidoException;
import cl.duoc.bancoxyz.processor.TransaccionProcessor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class TransaccionBatchConfig {

    private static final int CHUNK_SIZE = 10;

    //Reader
    @Bean
    public FlatFileItemReader<TransaccionDto> transaccionItemReader() {
        return new FlatFileItemReaderBuilder<TransaccionDto>()
                .name("transaccionItemReader")
                .resource(new ClassPathResource("data/semana_1/transacciones.csv"))
                .linesToSkip(1)
                .delimited()
                .names("id", "fecha", "monto", "tipo")
                .targetType(TransaccionDto.class)
                .build();
    }

    //Processor
    @Bean
    public TransaccionProcessor transaccionItemProcessor(){
        return new TransaccionProcessor();
    }

    //Writer
    @Bean
    public JdbcBatchItemWriter<Transaccion> transaccionItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaccion>()
                .dataSource(dataSource)
                .sql("INSERT INTO transacciones (id_legacy, fecha, monto, tipo) "
                        + "VALUES (:idLegacy, :fecha, :monto, :tipo)")
                .beanMapped()
                .build();
    }

    @Bean
    public PlatformTransactionManager transaccionTransactionManager(DataSource dataSource) {
        return new JdbcTransactionManager(dataSource);
    }

    @Bean
    public Step transaccionStep(JobRepository jobRepository,
                                PlatformTransactionManager transaccionTransactionManager,
                                FlatFileItemReader<TransaccionDto> transaccionItemReader,
                                TransaccionProcessor transaccionItemProcessor,
                                JdbcBatchItemWriter<Transaccion> transaccionItemWriter) {
        return new StepBuilder("transaccionStep", jobRepository)
                .<TransaccionDto, Transaccion>chunk(CHUNK_SIZE, transaccionTransactionManager)
                .reader(transaccionItemReader)
                .processor(transaccionItemProcessor)
                .writer(transaccionItemWriter)
                .faultTolerant()
                .skip(RegistroInvalidoException.class)
                .skip(FechaInvalidaException.class)
                .skipLimit(100)
                .build();
    }

    @Bean
    public Job transaccionJob(JobRepository jobRepository, Step transaccionStep) {
        return new JobBuilder("transaccionJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(transaccionStep)
                .build();
    }




}
