package com.example.Common_Kafka.Controller;

import com.example.Common_Kafka.Producer.Kafka_Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/JKafka")
public class ProcessCompletedController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessCompletedController.class);
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private Kafka_Producer kafkaProducerService;
    private RestTemplate restTemplate = new RestTemplate();

    private Queue<UUID> processIdQueue = new LinkedList<>();

    @PostMapping("/receiveProcessCompleted")
    public ResponseEntity<?> receiveProcessCompleted(@RequestBody Map<String, Object> requestBody) {
        logger.info("Received process completed message: {}", requestBody);

        String message = (String) requestBody.get("message");
        UUID process_id = UUID.fromString((String) requestBody.get("process_id"));

        if (message == null || process_id == null) {
            String errorMessage = "Invalid or missing message or process_id in the request body.";
            logger.error(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        } else {
            String responseMessage = "Received the process completed message successfully";
            sendMessage(message);
            System.out.println(responseMessage);
            System.out.println(process_id);
            addProcessIdToQueue(process_id);
            return ResponseEntity.ok(responseMessage);
        }
    }

    private void addProcessIdToQueue(UUID process_id) {
        synchronized (processIdQueue) {
            processIdQueue.offer(process_id);
        }
    }

    @Scheduled(fixedDelay = 10000) // Runs every 10 seconds
    public void sendProcessIdsToNode() {
        synchronized (processIdQueue) {
            while (!processIdQueue.isEmpty()) {
                UUID process_id = processIdQueue.poll();
                sendProcessIdToNode(process_id);
            }
        }
    }

    private ResponseEntity<String> sendProcessIdToNode(UUID process_id) {
        final int maxRetries = 3;
        int retryCount = 0;
        boolean success = false;

        while (!success && retryCount < maxRetries) {
            retryCount++;

            try {
                String url = "http://192.168.2.47:9000/nodeKafka/kafka/Response";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, UUID> requestBody = new HashMap<>();
                requestBody.put("process_id", process_id);

                HttpEntity<Map<String, UUID>> requestEntity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);

                if (responseEntity.getStatusCode() == HttpStatus.OK) {
                    String message2 = "Process with processID: " + process_id + " sent to Node";
                    logger.info("Process with processID: {} sent successfully to Node", process_id);
                    sendMessage(message2);
                    synchronized (processIdQueue) {
                        // Remove the process_id from the queue after successful processing
                        processIdQueue.remove(process_id);
                    }
                    success = true;
                } else if (responseEntity.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                    logger.error("Process with processID: {} failed with status code 500. Retrying... (Attempt {})", process_id, retryCount);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage(e.getMessage());
            }

            if (!success) {
                try {
                    // Sleep for a few seconds before the next retry
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!success) {
            String errorMessage = "Process with processID: " + process_id + " failed after " + maxRetries + " retries.";
            logger.error(errorMessage);
            sendMessage(errorMessage);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }

    public void sendMessage(String message) {
        kafkaProducerService.sendMessage("process-over", message);
    }
}
