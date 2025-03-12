# Processing Application

## Description

This is a simple Kafka producer application that exposes a `GET` endpoint `/betvictor/text` on port `8081`. The endpoint accepts two parameters:

- **p** (number of paragraphs)
- **t** (type - "hipster-centric" or "hipster-latin")

The application fetches paragraphs from an external endpoint based on these parameters, calculates statistics, and then:

1. Returns the calculated statistics as a response.
2. Sends the statistics to a Kafka topic that is created on application startup.

The application is configurable to use any Kafka broker, and the Kafka broker's connection details can be provided in the configuration file.

## Prerequisites

To run this application, you need the following:

- **Java 23**: The application requires Java 23 to run. Please ensure that Java 23 is installed on your system.
- **Docker** (to run Kafka in a container)
- **Docker Compose** (to orchestrate the Kafka container and the application)

## Setup

### 1. Start Kafka with Docker Compose

To start the Kafka container, run the following command:

```bash
docker-compose up -d 
```

This will bring up the Kafka container in detached mode.

### 2. Start the Spring Boot Application 

The application will listen on http://localhost:8081.


## Testing

To test the /betvictor/text endpoint, make a GET request to:

GET http://localhost:8081/betvictor/text?p=5&t=hipster-latin


