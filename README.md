Contains the code for the Batch Processing Cloud Apps with Java.

All the examples read from a flat file and write to a DB.

# Requirements

- Java 17
- MySQL running on port 3306
- Import the schema.sql file to the running MySQL instance

#Jobs

## SimpleJob

It's as the name say, a simple job with a flat file reader and a JdbcWriter. It's the base code for all others

`To run locally, start the Spring Boot app with the profile "simple".`

## MultithreadedJob

Same as the SimpleJob but each chunk will run on a separated thread.

`To run locally, start the Spring Boot app with the profile "multi".`

## AsyncJob

Will run with AsyncProcessor and AsyncWriters. 

`To run locally, start the Spring Boot app with the profile "async".`

## ManagerConfiguration and WorkerConfiguration

### To run this example you will also need RabbitMQ running on port 5672, and two durable queues (requests and responses).

Together they are an implementation of Remote Chunking model.
To run locally
- `start 1+ instances of the worker with the profile "worker".`
- `start 1 instance of the worker with the profile "manager".`



