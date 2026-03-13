package com.swifttrack.FeignClient;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.swifttrack.dto.tenantDto.TenantDeliveryConf;

@FeignClient(name = "TenantService", url = "http://localhost:8080/tenantservice")
public interface TenantInterface {

    @GetMapping("/tenant-delivery/v1/configure")
    ResponseEntity<List<TenantDeliveryConf>> getTenantDeliveryConfiguration(@RequestHeader String token);

    @GetMapping("/tenant-delivery/v1/configure/internal")
    ResponseEntity<List<TenantDeliveryConf>> getTenantDeliveryConfigurationByTenantId(@RequestParam UUID tenantId);
}
