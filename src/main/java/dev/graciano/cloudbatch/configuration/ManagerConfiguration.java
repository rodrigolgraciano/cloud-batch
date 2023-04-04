package dev.graciano.cloudbatch.configuration;

import dev.graciano.cloudbatch.domain.Rental;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.chunk.RemoteChunkingManagerStepBuilderFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.RecordFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;

@Configuration
@Profile("manager")
public class ManagerConfiguration {

  private final RemoteChunkingManagerStepBuilderFactory stepBuilderFactory;
  private BatchJobListener batchJobListener;


  public ManagerConfiguration(RemoteChunkingManagerStepBuilderFactory stepBuilderFactory, BatchJobListener batchJobListener) {
    this.stepBuilderFactory = stepBuilderFactory;
    this.batchJobListener = batchJobListener;
  }

  @Bean
  public Job remoteJob(JobRepository jobRepository) {
    return new JobBuilder("remoteJob", jobRepository)
      .start(stepOne())
      .listener(batchJobListener)
      .build();
  }

  @Bean
  public TaskletStep stepOne() {
    return stepBuilderFactory.get("remoteStep")
      .chunk(500)
      .reader(libraryReader(null))
      .outputChannel(requests())
      .inputChannel(responses())
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
  public DirectChannel requests() {
    return new DirectChannel();
  }

  @Bean
  public IntegrationFlow outboundFlow(AmqpTemplate amqpTemplate) {
    return IntegrationFlow.from(requests())
      .handle(Amqp.outboundAdapter(amqpTemplate)
        .routingKey("requests"))
      .get();
  }

  @Bean
  public QueueChannel responses() {
    return new QueueChannel();
  }

  @Bean
  public IntegrationFlow inboundFlow(ConnectionFactory connectionFactory) {
    return IntegrationFlow
      .from(Amqp.inboundAdapter(connectionFactory, "responses"))
      .channel(responses())
      .get();
  }
}
