package dev.graciano.cloudbatch.configuration;

import dev.graciano.cloudbatch.domain.Rental;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
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
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@Profile("async")
public class AsyncJobConfiguration {

  private final BatchJobListener batchJobListener;

  public AsyncJobConfiguration(BatchJobListener batchJobListener) {
    this.batchJobListener = batchJobListener;
  }

  @Bean
  public Job asyncJob(JobRepository jobRepository) {
    return new JobBuilder("asyncJob", jobRepository)
      .incrementer(new RunIdIncrementer())
      .start(asyncStep(null, null))
      .listener(batchJobListener)
      .build();
  }

  @Bean
  public Step asyncStep(JobRepository jobRepository,
                        PlatformTransactionManager transactionManager) {
    return new StepBuilder("asyncStep", jobRepository)
      .<Rental, Rental>chunk(100, transactionManager)
      .reader(libraryReader(null))
      .processor((ItemProcessor) asyncItemProcessor())
      .writer(asyncItemWriter())
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
  public ItemProcessor<Rental, Rental> processor() {
    return (rental) -> {
      Thread.sleep(5);
      return rental;
    };
  }

  @Bean
  public AsyncItemProcessor<Rental, Rental> asyncItemProcessor() {
    AsyncItemProcessor<Rental, Rental> processor = new AsyncItemProcessor<>();
    processor.setDelegate(processor());
    processor.setTaskExecutor(new SimpleAsyncTaskExecutor());

    return processor;
  }

  @Bean
  public AsyncItemWriter<Rental> asyncItemWriter() {
    AsyncItemWriter<Rental> writer = new AsyncItemWriter<>();

    writer.setDelegate(rentalWriter(null));

    return writer;
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
