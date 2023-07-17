# Time Delay Controller

The Time Delay Controller is a RESTful API controller that handles delayed processing of requests. It receives a process ID and initiates a delay before marking the process as completed and sending a process completion notification to another service.

## Installation

1. Clone the repository:

   ```shell
   git clone https://github.com/example/TimeDelay_Async.git
   ```

2. Build the project using your preferred build tool (e.g., Maven or Gradle).

3. Set up the necessary dependencies and configuration properties in the application configuration file.

## Usage

1. Run the application.

2. Send a POST request to the `/JKafka/delayed` endpoint with a JSON payload containing the following field:

   - `process_id`: The unique identifier for the process.

3. The controller will validate the request payload and check if the process with the provided ID has already been completed. If the process is already completed, an error message will be returned. Otherwise, the process will be marked as started, and a delay of 10 seconds will be initiated.

4. After the delay, the process will be marked as completed, and a process completion notification will be sent to the specified URL (`http://192.168.2.71:8081/JKafka/receiveProcessCompleted`) with the process ID and completion message.

5. If the process completion notification is successfully sent (HTTP status code 200), a success message will be logged. Otherwise, an error message will be logged.

## Configuration

Ensure that the application configuration file contains the necessary properties for configuring the server, endpoints, and any required dependencies.

## Dependencies

The project has the following dependencies:

- Spring Boot: [version]
- SLF4J: [version]
- RestTemplate: [version]

## Contributing

Contributions are welcome! If you would like to contribute to this project, please follow these guidelines:

1. Fork the repository.

2. Create a new branch for your feature or bug fix.

3. Make your changes and ensure that the code is properly formatted.

4. Write tests to cover your changes.

5. Submit a pull request explaining the changes you've made.

## License

This project is licensed under the [License Name]. Please see the [LICENSE](LICENSE) file for more information.

## Credits

- [Author Name] - [Mann Panchani (Full Stack Developer)]

Feel free to customize this README file based on your project's specific requirements and add any additional sections or information that may be relevant.

#############################################################################################################################

# Common Kafka - Process Completed Controller

This project includes a RESTful API controller `ProcessCompletedController` that handles the reception of process completion messages and sends them to a Kafka topic for further processing.

## Installation

1. Clone the repository:

   ```shell
   git clone  https://github.com/example/Common_Kafka.git
   ```

2. Build the project using your preferred build tool (e.g., Maven or Gradle).

3. Set up the Kafka configuration by providing the necessary properties in the application configuration file.

## Usage

1. Run the application.

2. Send a POST request to the `/JKafka/receiveProcessCompleted` endpoint with a JSON payload containing the following fields:

   - `message`: The message associated with the process completion.
   - `process_id`: The unique identifier for the completed process.

3. The controller will validate the request payload and, if valid, send the message to the Kafka topic "process-over" using the configured Kafka producer.

4. The received message will be logged, and the process ID will be added to a queue for further processing.

5. Every 10 seconds (configurable), the `sendProcessIdsToNode` method will be executed as a scheduled task, which retrieves process IDs from the queue and sends them to a specified node using a POST request.

6. The response from the node will be evaluated. If successful (HTTP status code 200), the process ID will be removed from the queue. If the response is an internal server error (HTTP status code 500), the request will be retried up to three times.

## Configuration

The application can be configured by modifying the appropriate properties in the application configuration file. Ensure that the Kafka properties, such as bootstrap servers and topic names, are correctly set.

## Dependencies

The project has the following dependencies:

- Spring Boot: [version]
- Spring Kafka: [version]
- SLF4J: [version]
- RestTemplate: [version]

## Contributing

Contributions are welcome! If you would like to contribute to this project, please follow these guidelines:

1. Fork the repository.

2. Create a new branch for your feature or bug fix.

3. Make your changes and ensure that the code is properly formatted.

4. Write tests to cover your changes.

5. Submit a pull request explaining the changes you've made.

## License

This project is licensed under the [License Name]. Please see the [LICENSE](LICENSE) file for more information.

## Credits

- [Author Name] - [Mann Panchani (Full Stack Developer)]

Feel free to customize this README file based on your project's specific requirements and add any additional sections or information that may be relevant.
