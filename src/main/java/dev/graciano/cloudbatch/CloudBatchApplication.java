package dev.graciano.cloudbatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EnableBatchIntegration
@SpringBootApplication
public class CloudBatchApplication {

	public static void main(String[] args) {
		List<String> strings = Arrays.asList(args);

		List<String> finalArgs = new ArrayList<>(strings.size() + 1);
		finalArgs.addAll(strings);
		finalArgs.add("libraryFile=lib10k.csv");

		SpringApplication.run(CloudBatchApplication.class,
			finalArgs.toArray(new String[finalArgs.size()]));
	}

}
