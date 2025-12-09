
package com.swifttrack.ProviderService.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import com.swifttrack.ProviderService.services.ProviderService;

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
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<String> getProviders() {
        return ResponseEntity.ok("Providers");
    }

}
