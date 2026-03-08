package com.swifttrack.BillingAndSettlementService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
		"com.swifttrack.BillingAndSettlementService",
		"com.swifttrack.http" // Scan common module's HTTP client utilities
})
@org.springframework.context.annotation.Import(com.swifttrack.BillingAndSettlementService.conf.EnvConfiguration.class)
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.swifttrack.FeignClient")
@org.springframework.cache.annotation.EnableCaching
public class BillingAndSettlementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillingAndSettlementServiceApplication.class, args);
	}

}
