
package com.swifttrack.ProviderService.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.swifttrack.ProviderService.dto.CreateProviderAndServicableAreas;
import com.swifttrack.ProviderService.dto.GetProviders;
import com.swifttrack.ProviderService.services.ProviderService;
import com.swifttrack.dto.Message;

@RestController
@RequestMapping("/api/providers")
@Tag(name = "Providers", description = "Provider management endpoints")
public class ProviderController {

    @Autowired
    private ProviderService providerService;

    @GetMapping("/v1/list")
    @Operation(summary = "Get all providers", description = "Retrieve list of all available providers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Providers retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal Server error")
    })
    public ResponseEntity<List<GetProviders>> getProviders() {
        System.out.println("Getting providers");
        return ResponseEntity.ok(providerService.getProviders());
    }

    @PostMapping("/v1/create")
    @Operation(summary = "Create a new provider", description = "Create a new provider with the given details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Provider created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal Server error")
    })
    public ResponseEntity<Message> createProvider(@RequestHeader String token,@RequestBody CreateProviderAndServicableAreas provider) {
        return ResponseEntity.ok(providerService.createProvider(token,provider));
    }

}
