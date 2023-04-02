package dev.graciano.cloudbatch.configuration;

import dev.graciano.cloudbatch.domain.Rental;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.batch.integration.chunk.RemoteChunkingWorkerBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;

import javax.sql.DataSource;

@Configuration
@Profile("worker")
public class WorkerConfiguration {

  private final RemoteChunkingWorkerBuilder<Rental, Rental> workerBuilder;

  public WorkerConfiguration(RemoteChunkingWorkerBuilder<Rental, Rental> workerBuilder) {
    this.workerBuilder = workerBuilder;
  }

  @Bean
  public IntegrationFlow integrationFlow() {
    return this.workerBuilder
      .itemProcessor(rentalProcessor())
      .itemWriter(writer(null))
      .inputChannel(requests())
      .outputChannel(responses())
      .build();
  }

  @Bean
  public ItemProcessor<Rental, Rental> rentalProcessor() {
    return r -> {
      System.out.println("Processing rental = " + r);
      return r;
    };
  }

  @Bean
  public DirectChannel requests() {
    return new DirectChannel();
  }

  @Bean
  public DirectChannel responses() {
    return new DirectChannel();
  }

  @Bean
  public IntegrationFlow inboundFlow(ConnectionFactory connectionFactory) {
    return IntegrationFlow
      .from(Amqp.inboundAdapter(connectionFactory, "requests"))
      .channel(requests())
      .get();
  }

  @Bean
  public IntegrationFlow outboundFlow(AmqpTemplate template) {
    return IntegrationFlow.from(responses())
      .handle(Amqp.outboundAdapter(template)
        .routingKey("responses"))
      .get();
  }

  @Bean
  public JdbcBatchItemWriter<Rental> writer(DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<Rental>()
      .dataSource(dataSource)
      .beanMapped()
      .sql("INSERT INTO RENTAL (ID, TITLE, ISBN, USER) VALUES (:id, :title, :isbn, :user)")
      .build();
  }
}
