package com.swifttrack.AIDispatchService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
public class AIDispatchServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AIDispatchServiceApplication.class, args);
	}
}
