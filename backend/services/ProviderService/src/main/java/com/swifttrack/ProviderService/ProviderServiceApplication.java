package com.swifttrack.ProviderService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
		"com.swifttrack.ProviderService",
		"com.swifttrack.http" // Scan common module's HTTP client utilities
})
@org.springframework.context.annotation.Import(com.swifttrack.ProviderService.conf.EnvConfiguration.class)
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.swifttrack.FeignClient")
public class ProviderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProviderServiceApplication.class, args);
	}

}