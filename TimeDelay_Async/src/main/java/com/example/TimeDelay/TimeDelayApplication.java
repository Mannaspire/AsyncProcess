package com.example.TimeDelay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class TimeDelayApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimeDelayApplication.class, args);
	}

}
