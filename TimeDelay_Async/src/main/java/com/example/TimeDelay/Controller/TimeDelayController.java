package com.example.TimeDelay.Controller;

import com.example.TimeDelay.Services.ProcessIdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/JKafka")
public class TimeDelayController {

    private static final Logger logger = LoggerFactory.getLogger(TimeDelayController.class);

    @Autowired
    private ProcessIdService processIdService;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/delayed")
    public ResponseEntity<?> asynchronousMethod(@RequestBody Map<String, Object> requestBody) {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String method = request.getMethod();
        String url = request.getRequestURI();

        logger.info("Request - Method: {}, URL: {}", method, url);
        logger.info("Request Body: {}", requestBody);

        String processIdString = (String) requestBody.get("process_id");

        if (processIdString == null) {
            String errorMessage = "Invalid or missing processId.";
            logger.error("Invalid or missing processId: {}", processIdString);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }

        UUID process_id;
        try {
            process_id = UUID.fromString(processIdString);
        } catch (IllegalArgumentException e) {
            String errorMessage = "Invalid processId format.";
            logger.error(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }

        if (processIdService.isProcessCompleted(process_id)) {
            String errorMessage = "Process with processID: " + process_id + " has already been completed.";
            logger.error(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        } else {
            String message1 = "Received processId: " + process_id + ". Processing started.";
            logger.info(message1);

            TimeDelayController.DelayThread delayThread = new TimeDelayController.DelayThread(process_id, requestBody);
            delayThread.start();

            return ResponseEntity.accepted().body(message1);
        }
    }

    private class DelayThread extends Thread {

        private UUID process_id;
        private Map<String, Object> requestBody;

        public DelayThread(UUID process_id, Map<String, Object> requestBody) {
            this.process_id = process_id;
            this.requestBody = requestBody;
        }

        @Override
        public void run() {
            try {
                logger.debug("Thread sleep for 10 seconds");
                Thread.sleep(10000);

                String message2 = "Process with processID: " + process_id + " completed successfully";
                logger.info("Process with processID: {} completed successfully", process_id);

                processIdService.markProcessCompleted(process_id);

                String url = "http://192.168.2.71:8081/JKafka/receiveProcessCompleted";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("process_id", process_id);
                requestBody.put("message", message2);

                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

                try {
                    ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);
                    if (responseEntity.getStatusCode() == HttpStatus.OK) {
                        logger.info("Process completion notification sent successfully");
                    } else {
                        logger.error("Failed to send process completion notification. Status code: {}",
                                responseEntity.getStatusCodeValue());
                    }
                } catch (HttpServerErrorException e) {
                    logger.error("Failed to send process completion notification. Server error: {}", e.getMessage());
                }

            } catch (InterruptedException ex) {
                String errorMessage = "Process with processId: " + process_id + " was interrupted.";
                logger.error("Process with processId: {} was interrupted.", process_id, ex);
                System.out.println(errorMessage);
                throw new RuntimeException(ex);
            }
        }
    }
}
