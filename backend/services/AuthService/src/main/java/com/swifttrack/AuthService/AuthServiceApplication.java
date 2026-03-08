package com.swifttrack.AuthService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@org.springframework.context.annotation.Import(com.swifttrack.AuthService.conf.EnvConfiguration.class)
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.swifttrack.FeignClient")
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

}
