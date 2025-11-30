package com.swifttrack.TenantService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.swifttrack.FeignClients")
@ComponentScan(basePackages = {"com.swifttrack"})
public class TenantServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TenantServiceApplication.class, args);
	}

}
