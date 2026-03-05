package com.swifttrack.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.swifttrack.DriverService.dto.spatial.AiDispatchRequest;
import com.swifttrack.DriverService.dto.spatial.AiDispatchResponse;

@FeignClient(name = "aiDispatchService", url = "http://localhost:8080/aidispatchservice")
public interface AIDispatchInterface {

    @PostMapping("/dispatch/assign")
    ResponseEntity<AiDispatchResponse> assign(@RequestBody AiDispatchRequest request);
}
