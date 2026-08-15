package cl.duoc.bancoxyz.config;

import cl.duoc.bancoxyz.dtos.CuentaBancariaDto;
import cl.duoc.bancoxyz.dtos.TransaccionDto;
import cl.duoc.bancoxyz.entities.CuentaBancaria;
import cl.duoc.bancoxyz.exception.FechaInvalidaException;
import cl.duoc.bancoxyz.exception.RegistroInvalidoException;
import cl.duoc.bancoxyz.processor.CuentaBancariaProcessor;
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
public class CuentaBancariaBatchConfig {

    private static final int CHUNK_SIZE = 10;

    //Reader
    @Bean
    public FlatFileItemReader<CuentaBancariaDto> cuentaBancariaItemReader() {
        return new FlatFileItemReaderBuilder<CuentaBancariaDto>()
                .name("cuentaBancariaItemReader")
                .resource(new ClassPathResource("data/semana_1/intereses.csv"))
                .linesToSkip(1)
                .delimited()
                .names("cuenta_id", "nombre", "saldo", "edad", "tipo")
                .targetType(CuentaBancariaDto.class)
                .build();
    }

    @Bean
    public CuentaBancariaProcessor cuentaBancariaItemProcessor(){
        return new CuentaBancariaProcessor();
    }

    //Writer
    @Bean
    public JdbcBatchItemWriter<CuentaBancaria> cuentaBancariaItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<CuentaBancaria>()
                .dataSource(dataSource)
                .sql("INSERT INTO cuentas_bancarias (cuenta_id_legacy, nombre, saldo, edad, tipo, interes, saldo_final) "
                        + "VALUES (:cuentaIdLegacy, :nombre, :saldo, :edad, :tipo, :interes, :saldoFinal)")
                .beanMapped()
                .build();
    }

    //Gestiona la integridad transaccional con la base de datos
    @Bean
    public PlatformTransactionManager cuentaBancariaTransactionManager(DataSource dataSource) {
        return new JdbcTransactionManager(dataSource);
    }

    //Step
    @Bean
    public Step cuentaBancariaStep(JobRepository jobRepository,
                                   PlatformTransactionManager cuentaBancariaTransactionManager,
                                   FlatFileItemReader<CuentaBancariaDto> cuentaBancariaItemReader,
                                   CuentaBancariaProcessor cuentaBancariaItemProcessor,
                                   JdbcBatchItemWriter<CuentaBancaria> cuentaBancariaItemWriter) {
        return new StepBuilder("cuentaBancariaStep", jobRepository)
                .<CuentaBancariaDto, CuentaBancaria>chunk(CHUNK_SIZE, cuentaBancariaTransactionManager)
                .reader(cuentaBancariaItemReader)
                .processor(cuentaBancariaItemProcessor)
                .writer(cuentaBancariaItemWriter)
                .faultTolerant()
                .skip(RegistroInvalidoException.class)
                .skip(FechaInvalidaException.class)
                .skipLimit(100)
                .build();
    }

    //Job
    @Bean
    public Job cuentaBancariaJob(JobRepository jobRepository, Step cuentaBancariaStep) {
        return new JobBuilder("cuentaBancariaJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(cuentaBancariaStep)
                .build();
    }

}
