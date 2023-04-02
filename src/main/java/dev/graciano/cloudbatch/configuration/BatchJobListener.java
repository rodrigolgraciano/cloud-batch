package dev.graciano.cloudbatch.configuration;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class BatchJobListener {

  private final JdbcTemplate jdbcTemplate;

  public BatchJobListener(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @BeforeJob
  public void beforeJob(JobExecution jobExecution){

    Long count = jdbcTemplate.queryForObject("select count(*) from RENTAL", Long.class);
    System.out.println("Table RENTAL count is " + count);

    System.out.println("Dropping table");
    jdbcTemplate.execute("drop table RENTAL");

    System.out.println("Recreating table");
    jdbcTemplate.execute("CREATE TABLE RENTAL  (\n" +
      "    ID BIGINT NOT NULL PRIMARY KEY ,\n" +
      "\tTITLE VARCHAR(250) NOT NULL ,\n" +
      "\tISBN VARCHAR(20) NOT NULL ,\n" +
      "\tUSER VARCHAR(250) NOT NULL);");

    count = jdbcTemplate.queryForObject("select count(*) from RENTAL", Long.class);
    System.out.println("Table RENTAL count is " + count);
  }
}
