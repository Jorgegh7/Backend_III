package cl.duoc.bancoxyz.config;

import cl.duoc.bancoxyz.dtos.MovimientoAnualDto;
import cl.duoc.bancoxyz.entities.MovimientoAnual;
import cl.duoc.bancoxyz.exception.FechaInvalidaException;
import cl.duoc.bancoxyz.exception.RegistroInvalidoException;
import cl.duoc.bancoxyz.processor.MovimientoAnualProcessor;
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
public class MovimientoAnualBatchConfig {

    private static final int CHUNK_SIZE = 10;

    //Reader
    @Bean
    public FlatFileItemReader<MovimientoAnualDto> movimientoAnualItemReader() {
        return new FlatFileItemReaderBuilder<MovimientoAnualDto>()
                .name("movimientoAnualItemReader")
                .resource(new ClassPathResource("data/semana_1/cuentas_anuales.csv"))
                .linesToSkip(1)
                .delimited()
                .names("cuenta_id", "fecha", "transaccion", "monto", "descripcion")
                .targetType(MovimientoAnualDto.class)
                .build();
    }

    //Processor
    @Bean
    public MovimientoAnualProcessor movimientoAnualProcessor(){
        return new MovimientoAnualProcessor();
    }

    //Writer
    @Bean
    public JdbcBatchItemWriter<MovimientoAnual> movimientoAnualItemWriter(DataSource dataSource){
        return new JdbcBatchItemWriterBuilder<MovimientoAnual>()
                .dataSource(dataSource)
                .sql("INSERT INTO movimientos_anuales (cuenta_id_legacy, fecha, transaccion, monto, descripcion) "
                        + "VALUES (:cuentaIdLegacy, :fecha, :transaccion, :monto, :descripcion)")
                .beanMapped()
                .build();
    }

    @Bean
    public PlatformTransactionManager movimientoAnualTransactionManager(DataSource dataSource) {
        return new JdbcTransactionManager(dataSource);
    }

    @Bean
    public Step movimientoAnualStep(JobRepository jobRepository,
                                    PlatformTransactionManager movimientoAnualTransactionManager,
                                    FlatFileItemReader<MovimientoAnualDto> movimientoAnualItemReader,
                                    MovimientoAnualProcessor movimientoAnualItemProcessor,
                                    JdbcBatchItemWriter<MovimientoAnual> movimientoAnualItemWriter) {
        return new StepBuilder("movimientoAnualStep", jobRepository)
                .<MovimientoAnualDto, MovimientoAnual>chunk(CHUNK_SIZE, movimientoAnualTransactionManager)
                .reader(movimientoAnualItemReader)
                .processor(movimientoAnualItemProcessor)
                .writer(movimientoAnualItemWriter)
                .faultTolerant()
                .skip(RegistroInvalidoException.class)
                .skip(FechaInvalidaException.class)
                .skipLimit(100)
                .build();
    }

    @Bean
    public Job movimientoAnualJob(JobRepository jobRepository, Step movimientoAnualStep) {
        return new JobBuilder("movimientoAnualJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(movimientoAnualStep)
                .build();
    }

}
