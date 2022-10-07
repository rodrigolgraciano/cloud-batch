package dev.graciano.cloudbatch.configuration;

import dev.graciano.cloudbatch.domain.Rental;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.RecordFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@Profile("multi")
public class MultithreadedJobConfiguration {

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;

  public MultithreadedJobConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
  }

  @Bean
  public Job multithreadedJob(){
    return jobBuilderFactory
      .get("multithreadedJob")
      .incrementer( new RunIdIncrementer())
      .start(multithreadedStep())
      .build();
  }

  @Bean
  public Step multithreadedStep(){
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(4);
    executor.afterPropertiesSet();

    return stepBuilderFactory
      .get("multithreadedStep")
      .<Rental,Rental>chunk(100)
      .reader(libraryReader(null))
      .writer(rentalWriter(null))
      .taskExecutor(executor)
      .build();
  }

  @Bean
  @StepScope
  public FlatFileItemReader<Rental> libraryReader(
    @Value("#{jobParameters['libraryFile']}") Resource resource) {

    return new FlatFileItemReaderBuilder<Rental>()
      .saveState(false)
      .resource(resource)
      .delimited()
      .names("id", "title", "isbn", "user")
      .fieldSetMapper(new RecordFieldSetMapper<>(Rental.class))
      .build();
  }

  @Bean
  public JdbcBatchItemWriter<Rental> rentalWriter(DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<Rental>()
      .dataSource(dataSource)
      .beanMapped()
      .sql("INSERT INTO RENTAL (ID, TITLE, ISBN, USER) VALUES (:id, :title, :isbn, :user)")
      .build();
  }
}
