package com.swifttrack.AIDispatchService.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.AIDispatchService.dto.DispatchRequest;
import com.swifttrack.AIDispatchService.dto.DispatchResponse;
import com.swifttrack.AIDispatchService.services.DispatchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dispatch")
@Tag(name = "AI Dispatch", description = "AI-based driver selection using LLM inference with RAG context")
@Validated
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;

    @PostMapping("/assign")
    @Operation(
            summary = "Assign optimal driver",
            description = "Receives a list of candidate driver IDs (pre-filtered by KD-tree proximity) " +
                    "and uses LLM-based analysis with RAG context to select the optimal driver for dispatch. " +
                    "Returns a structured JSON response with driver_id, confidence score, and technical reasoning.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver selected successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (empty driver list or > 5 drivers)"),
            @ApiResponse(responseCode = "500", description = "Pipeline error — fallback response returned")
    })
    public ResponseEntity<DispatchResponse> assignDriver(@Valid @RequestBody DispatchRequest request) {
        DispatchResponse response = dispatchService.dispatch(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Service health check endpoint")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI Dispatch Service is running");
    }
}
