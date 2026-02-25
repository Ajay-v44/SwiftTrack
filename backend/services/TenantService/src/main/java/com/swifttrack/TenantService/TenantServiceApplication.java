package com.swifttrack.TenantService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@org.springframework.context.annotation.Import(com.swifttrack.TenantService.conf.EnvConfiguration.class)
@EnableFeignClients(basePackages = "com.swifttrack.FeignClients")
@ComponentScan(basePackages = { "com.swifttrack" })
@EnableJpaRepositories(basePackages = "com.swifttrack.repositories")
@EntityScan(basePackages = "com.swifttrack.Models")
public class TenantServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TenantServiceApplication.class, args);
	}

}