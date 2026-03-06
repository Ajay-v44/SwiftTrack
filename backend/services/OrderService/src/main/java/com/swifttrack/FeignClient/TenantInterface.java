package com.swifttrack.FeignClient;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.swifttrack.dto.tenantDto.TenantDeliveryConf;

@FeignClient(name = "TenantService", url = "http://localhost:8080/tenantservice")
public interface TenantInterface {

    @GetMapping("/tenant-delivery/v1/configure")
    ResponseEntity<List<TenantDeliveryConf>> getTenantDeliveryConfiguration(@RequestHeader String token);
}
